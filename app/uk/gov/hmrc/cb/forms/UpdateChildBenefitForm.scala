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

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

/**
 * Created by adamconder on 04/05/2016.
 */

object UpdateChildBenefitForm {

  case class UpdateChildBenefitPageModel(updateChildBenefit: Option[Boolean])

  val form : Form[UpdateChildBenefitPageModel] = Form(
    mapping(
      "updateChildBenefit" -> optional(boolean).verifying(Messages("cb.error.update.child.benefit.required"), x => x.isDefined)
    )(UpdateChildBenefitPageModel.apply)(UpdateChildBenefitPageModel.unapply)
  )

}
