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

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import uk.gov.hmrc.cb.config.FrontendAppConfig

/**
 * Created by adamconder on 27/05/2016.
 */
object Constraints {
  /**
   * This will match:
   *  ^                beginning of string
   * [A-Za-z']+        one or more letters or apostrophe
   * ( [A-Za-z']+)*    zero or more instances of (a single space followed by one or more letters or apostrophe)
   * $                 end of string
   */
  val nameConstraint = "^[A-Za-z'-]+( [A-Za-z'-]+)*$"
  // for hellotest page
  val helloTestNameConstraint = "^[A-Za-z'-]+( [A-Za-z'-]+)*$"

  val dateFormatWithoutTimestamp = DateTimeFormat.forPattern("yyyy-MM-dd")
  val dateFormatWithTimestamp = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")

  private def removeTimeStamp(date : DateTime) : DateTime = {
    val todayWithoutTimestamp = dateFormatWithoutTimestamp.print(date)
    val todayDateWithoutTimestamp = DateTime.parse(todayWithoutTimestamp, dateFormatWithoutTimestamp)
    todayDateWithoutTimestamp
  }

  def dateIsNotAFutureDate(date : DateTime, today : DateTime = DateTime.now()) = {
    // remove timestamps and compare
    val todayDateWithoutTimestamp = removeTimeStamp(today)
    val dateOfBirthWithoutTimestamp = removeTimeStamp(date)

    dateOfBirthWithoutTimestamp.isBefore(todayDateWithoutTimestamp) || dateOfBirthWithoutTimestamp.isEqual(todayDateWithoutTimestamp)
  }

  def dateOfBirthIsEqualToOrAfterChildAgeLimit(date : DateTime, today : DateTime = DateTime.now()) = {
    val years = FrontendAppConfig.dateOfBirthAgeLimit
    val todayMinusYears = today.minusYears(years)
    // remove timestamps and compare
    val todayDateWithoutTimestamp = removeTimeStamp(todayMinusYears)
    val dateOfBirthWithoutTimestamp = removeTimeStamp(date)

    dateOfBirthWithoutTimestamp.isAfter(todayDateWithoutTimestamp) || dateOfBirthWithoutTimestamp.isEqual(todayDateWithoutTimestamp)
  }


}
