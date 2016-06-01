package uk.gov.hmrc.cb.controllers.child

import org.scalatest.mock.MockitoSugar
import play.api.data.Form
import play.api.mvc.Action
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.forms.ChildNameForm
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._
import org.mockito.Matchers.{eq => mockEq, _}

import scala.concurrent.Future

/**
 * Created by adamconder on 31/05/2016.
 */
class ChildNameControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  val getRequest = FakeRequest("GET", "/child-benefit/children/1/name")
  def postRequest(form: Form[ChildNamePageModel]) = FakeRequest("POST", "/child-benefit/children/1/name").withFormUrlEncodedBody(form.data.toSeq: _*)

  class ChildBenefitKeystoreService {
    def loadChildren() = ???
    def saveChildren(children : List[Child]) = ???
  }

  val mockController = new ChildNameController {
    override val authConnector = mock[AuditConnector]
    override val cacheClient = mock[ChildBenefitKeystoreService]
  }

  def redirectLocation()(implicit response : Action[_]) = response.header.headers.get("Location").get

  "ChildNameController" when {

    "initialising" should {

      "wire up the dependencies correctly" in {
        ChildNameController.authConnector shouldBe a[AuthConnector]
        ChildNameController.cacheClient shouldBe a[ChildBenefitKeystoreService]
      }

    }

    "calling /child-benefit/children/1/name" should {

      "respond to GET /child-benefit/children/1/name" in {
        val result = route(FakeRequest("GET", "/child-benefit/children/1/name"))
        status(result.get) should not be NOT_FOUND
      }

      "respond to POST /child-benefit/children/1/name" in {
        val result = route(FakeRequest("POST", "/child-benefit/children/1/name"))
        status(result.get) shouldBe SEE_OTHER
        result.get.header.headers("Location") should include("/confirmation")
      }

    }

    "get" should {

      "redirect to technical difficulties when keystore is down" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/technical-difficulties"
      }

      "respond 200 when no children in keystore" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(None))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when multiple children in keystore" in {
        val children = Some(List(Child(id = 1), Child(id = 2)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe OK
      }

      "redirect to technical difficulties when out of bounds exception" in {
        val children = Some(List(Child(id = 2)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/technical-difficulties"
      }

      "redirect to technical difficulties when out of bounds exception - empty list" in {
        val children = Some(List())
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get()(getRequest))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/technical-difficulties"
      }

    }

    "post" should {

      "redirect to technical difficulties when keystore is down when saving" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        when(mockController.cacheClient.saveChildren(mockEq(children))(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form)
        val result = await(mockController.post(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/technical-difficulties"
      }

      "redirect to technical difficulties when keystore is down when fetching children" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form)
        val result = await(mockController.post(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/technical-difficulties"
      }

      "respond BAD_REQUEST when POST is unsuccessful" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()).thenReturn(Future.successful(children))
        val form = ChildNameForm.form.fill(ChildNamePageModel("", "@£%!@£@£"))
        val request = postRequest(form)
        val result = await(mockController.post(request))
        status(result) shouldBe BAD_REQUEST
      }

      "redirect to confirmation - No children" in {
        val children = Some(List(Child(id = 1)))
        when(mockController.cacheClient.loadChildren()).thenReturn(Future.successful(None))
        when(mockController.cacheClient.saveChildren(mockEq(children.get))(any(), any())).thenReturn(Future.successful(children))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form)
        val result = await(mockController.post(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation when changing a child" in {
        val load = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Fenwick"))))
        when(mockController.cacheClient.loadChildren()).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save.get))(any(), any())).thenReturn(Future.successful(save))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Fenwick"))
        val request = postRequest(form)
        val result = await(mockController.post(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/confirmation"
      }

      "redirect to confirmation when there is no change" in {
        val load = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        val save = Some(List(Child(id = 1, firstname = Some("Adam"), surname = Some("Conder"))))
        when(mockController.cacheClient.loadChildren()).thenReturn(Future.successful(load))
        when(mockController.cacheClient.saveChildren(mockEq(save.get))(any(), any())).thenReturn(Future.successful(save))
        val form = ChildNameForm.form.fill(ChildNamePageModel("Adam", "Conder"))
        val request = postRequest(form)
        val result = await(mockController.post(request))
        status(result) shouldBe SEE_OTHER
        redirectLocation() shouldBe "/child-benefit/confirmation"
      }

    }

  }

}
