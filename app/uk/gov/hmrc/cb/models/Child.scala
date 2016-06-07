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

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.cb.forms.ChildNameForm.ChildNamePageModel
import uk.gov.hmrc.cb.mappings.Genders

/**
  * Created by chrisianson on 26/05/16.
  */
case class Child (
                 id: Short,
                 birthCertificateReference: Option[String] = None,
                 firstname: Option[String] = None,
                 surname: Option[String] = None,
                 dob: Option[LocalDate] = None,
                 gender: Genders.Gender = Genders.None,
                 previousClaim: Boolean = false
                 ) {

  def editFullName(firstName: String, lastName: String) = {
    this.copy(firstname = Some(firstName), surname = Some(lastName))
  }

  def editUniqueReferenceNumber(birthCertificateReference: String) = {
    this.copy(birthCertificateReference = Some(birthCertificateReference))
  }
}

object Child {
  implicit val formats = Json.format[Child]
}
