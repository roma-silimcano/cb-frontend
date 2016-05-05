/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cb

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.controllers.UpdateChildBenefitController
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
/**
  * Created by chrisianson on 04/05/16.
  */
class UpdateChildBenefitControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  val endPoint: String = "/update-child-benefit"

  def mockUpdateChildBenefitController = new UpdateChildBenefitController {
    override protected def authConnector: AuthConnector = mock[AuthConnector]
  }

  val fakeRequest = FakeRequest("GET", endPoint)

  s"GET $endPoint" should {

    "return 200" in {
      mockUpdateChildBenefitController
      val result = UpdateChildBenefitController.present(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return html content type" in {
      val result = UpdateChildBenefitController.present(fakeRequest)
    }

  }

}
