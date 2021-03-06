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

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.hmrc.cb.config.FrontendAppConfig
import uk.gov.hmrc.cb.forms.constraints.Constraints

/**
 * Created by adamconder on 27/05/2016.
 */
object ChildNameForm {

  case class ChildNamePageModel(firstName : String, lastName : String)

  private def validate(x : String) : Boolean = {
    lazy val lengthConstraint : Int = FrontendAppConfig.childLengthMaxConstraint
    val valid = x.nonEmpty && x.length <= lengthConstraint && x.matches(Constraints.nameConstraint)
    if (!valid) {
      Logger.info(s"[ChildNameForm][Validate] invalid name specified: $x")
    }
    valid
  }

  val form : Form[ChildNamePageModel] = Form(
    mapping(
        "firstName" -> text.verifying(Messages("cb.child.name.invalid"), validate _),
      "lastName" -> text.verifying(Messages("cb.child.name.invalid"), validate _)
    )(ChildNamePageModel.apply)(ChildNamePageModel.unapply)
  )

}
