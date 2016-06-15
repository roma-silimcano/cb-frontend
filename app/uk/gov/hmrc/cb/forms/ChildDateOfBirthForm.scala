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

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.i18n.Messages
import uk.gov.hmrc.cb.config.FrontendAppConfig
import uk.gov.hmrc.cb.forms.constraints._
import uk.gov.hmrc.play.mappers.DateTuple._

/**
 * Created by adamconder on 13/06/2016.
 */
object ChildDateOfBirthForm {

//  trait ChildPageModel {
//    val dateOfBirth : DateTime
//    val name : Option[String] = None
//  }

  case class ChildDateOfBirthPageModel(dateOfBirth : DateTime)

  private def futureDateConstraint : Constraint[DateTime] = Constraint("cb.child.date.of.birth.future"){
    model =>
      if(Constraints.dateIsNotAFutureDate(model)) Valid
      else Invalid(Messages("cb.child.date.of.birth.future"))
  }

  private def moreThanChildsAgeLimitConstraint : Constraint[DateTime] = Constraint("cb.child.date.of.birth.more.than.age.limit"){
    model =>
      if (Constraints.dateOfBirthIsEqualToOrAfterChildAgeLimit(model)) Valid
      else Invalid(Messages("cb.child.date.of.birth.more.than.age.limit", FrontendAppConfig.dateOfBirthAgeLimit))
  }

  val form : Form[ChildDateOfBirthPageModel] = Form(
    mapping(
      "dateOfBirth" -> validDateTuple
        .verifying(futureDateConstraint)
        .verifying(moreThanChildsAgeLimitConstraint)
    )(ChildDateOfBirthPageModel.apply)(ChildDateOfBirthPageModel.unapply)
  )

}
