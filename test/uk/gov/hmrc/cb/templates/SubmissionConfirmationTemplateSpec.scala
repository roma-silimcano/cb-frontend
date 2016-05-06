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

package uk.gov.hmrc.cb.templates

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import uk.gov.hmrc.cb.views
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import play.api.test.Helpers._

/**
 * Created by adamconder on 05/05/2016.
 */
class SubmissionConfirmationTemplateSpec extends UnitSpec with WithFakeApplication {

  "SubmissionConfirmation Template" should {

    "render title" in {
      val name = "Adam"
      val template = views.html.confirmation_submission(name = name)(FakeRequest("GET", ""))
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("page-title").text() shouldBe "Thank you for your submission"
    }

    "render the confirmation message" in {
      val name = "Adam"
      val template = views.html.confirmation_submission(name = name)(FakeRequest("GET", ""))
      val doc = Jsoup.parse(contentAsString(template))
      doc.getElementById("confirmation-message").text() shouldBe "Thank you Adam for your submission, we'll contact you by email with your result."
    }

  }

}
