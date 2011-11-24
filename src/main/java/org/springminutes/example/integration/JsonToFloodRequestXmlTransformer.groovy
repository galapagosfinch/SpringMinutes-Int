package org.springminutes.example.integration

import org.springframework.integration.transformer.AbstractPayloadTransformer
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import org.joda.time.DateTime

class JsonToFloodRequestXmlTransformer extends AbstractPayloadTransformer<String, String> {
    @Override
    protected String transformPayload(String t) {
        def json = (new JsonSlurper()).parseText(t);
        def writer = new StringWriter();
        def xml = new MarkupBuilder(writer);

        xml.REQUEST_GROUP(MISMOVersionID: "2.4") {
            SUBMITTING_PARTY(LoginAccountIdentifier: "${json.credentials.username}",
                    LoginAccountPassword: "${json.credentials.password}")
            REQUEST(InternalAccountIdentifier: "${json.credentials.username}",
                    RequestDatetime: "${DateTime.now()}") {
                REQUEST_DATA() {
                    FLOOD_REQUEST(MISMOVersionID: "2.4", _ActionType: "Original") {
                        _PRODUCT(_CategoryDescription: "Flood") {
                            _NAME(_Identifier: "FL")
                        }
                        BORROWER(_FirstName: "${json.loanInfo.borrower.firstName}",
                                _LastName: "${json.loanInfo.borrower.lastName}")
                        MORTGAGE_TERMS(LenderCaseIdentifier: "${json.loanInfo.caseNumber}")
                        PROPERTY(_StreetAddress: "${json.loanInfo.property.address1}",
                                _City: "${json.loanInfo.property.city}",
                                _State: "${json.loanInfo.property.state}",
                                _PostalCode: "${json.loanInfo.property.zip}") {}
                    }
                }
            }
        }

        return writer.toString()
    }
}
