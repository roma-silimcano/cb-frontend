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

package uk.gov.hmrc.cb.models

import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

/**
 * Created by adamconder on 06/05/2016.
 */
class ClaimantSpec extends UnitSpec with WithFakeApplication {

  "Claimant" should {

    "instantiate an instance of Claimant" in {
      val claimant = Claimant(name = "Louise", reference = 12345)
      claimant shouldBe a[Claimant]
      claimant.name shouldBe "Louise"
      claimant.reference shouldBe 12345
    }

  }

}
