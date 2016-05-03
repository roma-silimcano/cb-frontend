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

package uk.gov.hmrc.cb.config

import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class ApplicationConfigSpec extends UnitSpec with WithFakeApplication {

  "Application Config" must {

    "load google analytics properties file" in {
      val appConfig = FrontendAppConfig
      appConfig.analyticsToken shouldBe "N/A"
    }

    "load assets 2.209.0" in {
      val appConfig = FrontendAppConfig
      appConfig.assetsPrefix shouldBe "http://localhost:9032/assets/2.209.0"
    }

  }
}
  