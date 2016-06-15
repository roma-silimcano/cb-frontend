package uk.gov.hmrc.cb.controllers.child

import org.joda.time.{LocalDate, DateTime}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.data.Form
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.cb.controllers.session.CBSessionProvider
import uk.gov.hmrc.cb.forms.ChildDateOfBirthForm.ChildDateOfBirthPageModel
import uk.gov.hmrc.cb.managers.ChildrenManager
import uk.gov.hmrc.cb.models.Child
import uk.gov.hmrc.cb.service.keystore.CBKeystoreKeys
import uk.gov.hmrc.cb.service.keystore.KeystoreService.ChildBenefitKeystoreService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._

import scala.concurrent.Future

/**
 * Created by adamconder on 15/06/2016.
 */

class ChildDateOfBirthControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  val childIndex = 1

  val mockController = new ChildDateOfBirthController {
    override val cacheClient  = mock[ChildBenefitKeystoreService with CBKeystoreKeys]
    override val childrenService = ChildrenManager.childrenService
    override val authConnector = mock[AuthConnector]
  }

  "ChildDateOfBirthController" when {

    implicit lazy val getRequest = FakeRequest("GET", "/child-benefit/children/1/date-of-birth").withSession(CBSessionProvider.generateSessionId())
    def postRequest(form: Form[ChildDateOfBirthPageModel], index : Int) = FakeRequest("POST", s"/child-benefit/children/$index/date-of-birth")
                    .withFormUrlEncodedBody(form.data.toSeq: _*)
                    .withSession(CBSessionProvider.generateSessionId())

    implicit lazy val hc = HeaderCarrier()

    "initialising" should {

      "wire up the dependencies correctly" in {
        ChildDateOfBirthController.authConnector shouldBe a[AuthConnector]
        ChildDateOfBirthController.cacheClient shouldBe a[ChildBenefitKeystoreService]
      }

    }

    "calling /child-benefit/children/1/date-of-birth" should {

      "respond to GET /child-benefit/children/1/date-of-birth" in {
        val result = route(getRequest)
        result should not be NOT_FOUND
      }

      "respond to POST /child-benefit/children/1/date-of-birth" in {
        val result = route(FakeRequest("POST", "/child-benefit/children/1/date-of-birth"))
        result should not be NOT_FOUND
      }

    }

    "GET" should {

      "redirect to technical-difficulties page when keystore is down" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.failed(new RuntimeException))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe SEE_OTHER
        result.header.headers("LOCATION") should include("/technical-difficulties")
      }

      "respond 200 when no children in keystore" in {
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(Nil))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore with no date of birth" in {
        val children = List(Child(id = 1))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
      }

      "respond 200 when child in keystore" in {
        val dob = LocalDate.now
        val children = List(Child(id = 1, dob = Some(dob)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        status(result) shouldBe OK
        bodyOf(result) should include(dob.getDayOfMonth)
        bodyOf(result) should include(dob.getMonthOfYear)
        bodyOf(result) should include(dob.getYear)
      }

      "result 200 when multiple children in keystore" in {
        val dob = LocalDate.now
        val children = List(Child(id = 1, dob = Some(dob)), Child(id = 2, dob = Some(dob)))
        when(mockController.cacheClient.loadChildren()(any(), any())).thenReturn(Future.successful(children))
        val result = await(mockController.get(childIndex)(getRequest))
        statuts(result) shouldBe OK
      }

    }

  }

}
