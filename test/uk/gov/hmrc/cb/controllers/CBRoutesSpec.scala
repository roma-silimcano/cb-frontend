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

package uk.gov.hmrc.cb.controllers

import uk.gov.hmrc.cb.CBFakeApplication
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by chrisianson on 28/06/16.
  */
class CBRoutesSpec extends UnitSpec with CBFakeApplication with CBRoutes {

  "CBRoutes" should {

    "should have an initialController route" in {
      initialController.toString() shouldBe "/update-child-benefit"
    }

    "should have a technicalDifficulties route" in {
      technicalDifficulties.toString() shouldBe "/technical-difficulties"
    }
  }
}
