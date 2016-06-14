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

package uk.gov.hmrc.cb.forms

import java.util.Calendar

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.i18n.Messages
import uk.gov.hmrc.cb.config.FrontendAppConfig
import uk.gov.hmrc.cb.forms.constraints.Constraints
import uk.gov.hmrc.play.mappers.DateTuple._

/**
 * Created by adamconder on 13/06/2016.
 */
object ChildDateOfBirthForm {

  case class ChildDateOfBirthPageModel(dateOfBirth : DateTime)

  private def dateIsNotAFutureDate(date : DateTime) = {
    val today = DateTime.now()
    val todayDate = Constraints.dateFormat.parseDateTime(today.toString).toLocalDate
    !(date.toDate.compareTo(todayDate.toDate) > 0) // date is not after today
  }

  private def dateOfBirthIsLessThanAgeLimit(date : DateTime) = {
    val years = FrontendAppConfig.dateOfBirthAgeLimit
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, -years)
    val limit = calendar.getTime

    !(date.toDate.compareTo(limit) > 0) // date is not after limit
  }

  private def futureDateConstraint : Constraint[ChildDateOfBirthPageModel] = Constraint("cc.child.date.of.birth.future"){
    model =>
      if(dateIsNotAFutureDate(model.dateOfBirth)) Valid
      else Invalid(Messages("cc.child.date.of.birth.future"))
  }

  private def moreThan20YearsConstraint : Constraint[ChildDateOfBirthPageModel] = Constraint("cc.child.date.of.birth.more.than.age.limit"){
    model =>
      if (dateOfBirthIsLessThanAgeLimit(model.dateOfBirth)) Valid
      else Invalid(Messages("cc.child.date.of.birth.more.than.age.limit"))
  }

  val form : Form[ChildDateOfBirthPageModel] = Form(
    mapping(
      "dateOfBirth" -> validDateTuple
    )(ChildDateOfBirthPageModel.apply)(ChildDateOfBirthPageModel.unapply)
      .verifying(futureDateConstraint)
      .verifying(moreThan20YearsConstraint)
  )

}
