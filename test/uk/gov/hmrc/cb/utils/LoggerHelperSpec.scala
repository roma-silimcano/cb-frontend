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

package uk.gov.hmrc.cb.utils


import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by chrisianson on 08/06/16.
  */
class LoggerHelperSpec extends UnitSpec with WithFakeApplication {

  "LogggerHelper" should {

    "have an instance of LoggerHelper" in {
      val loggerHelper = new LoggerHelper
      loggerHelper shouldBe a[LoggerHelper]
    }

    "have a config value to toggle the logger setting" in {
      val configValue = LoggerHelper.loggerStatus
      configValue shouldBe a[java.lang.Boolean]
    }

    "set the correct reference when passed in a class name and method name" in {
      val loggerHelper = new LoggerHelper("TestClass", "get")
      loggerHelper.loggerReference shouldBe "[TestClass][get]"
    }
  }
}
