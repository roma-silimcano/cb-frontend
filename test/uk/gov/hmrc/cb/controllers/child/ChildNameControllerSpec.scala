package uk.gov.hmrc.cb.controllers.child

import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._

/**
 * Created by adamconder on 31/05/2016.
 */
class ChildNameControllerSpec extends UnitSpec with CBFakeApplication with MockitoSugar {

  val mockChildNameController = new ChildNameController {
    override val authConnector = mock[AuditConnector]
  }

  "ChildNameController" should {

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

}
