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

package uk.gov.hmrc.cb.forms.constraints

import java.util.regex.Pattern

/**
 * Created by adamconder on 27/05/2016.
 */
object Constraints {

//  val pattern = Pattern.compile("([a-zA-Z])\\w\\p{L}+", Pattern.UNICODE_CHARACTER_CLASS)

  /**
   * This will match:
   *  ^                beginning of string
   * [A-Za-z']+        one or more letters or apostrophe
   * ( [A-Za-z']+)*    zero or more instances of (a single space followed by one or more letters or apostrophe)
   * $                 end of string
   */
  val nameConstraint = "^[A-Za-z'-]+( [A-Za-z'-]+)*$"
}
