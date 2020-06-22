var fs = require('fs');
const jsrsasign = require('jsrsasign')
const { URLSearchParams } = require('url')
const { v4: uuidv4 } = require('uuid');
var access_Token = "";
const fetch = require('node-fetch')

fs.readFile('Users/richardbraman/IdeaProjects/dpc-app/src/main/resources/sorted_attributions.csv', 'utf8', function(err, data) {
    if (err) throw err;
    console.log(csvJSON(data));
});

async function csvJSON(csv){
    var lines=csv.split("\n");
    //var result = [];
    var CurrentPractionerID = "";
    var PractionerID = "";
    var GroupCount = 1;
    var PatientId = "";
    var GroupResource = "";
    var memberPatient = "";

    for(var i=0;i<lines.length -1;i++){  //for every line

        //var obj = {};
        var currentline=lines[i].split(","); //get the data
        PractionerID = currentline[1].replace(/(\r\n|\n|\r)/gm, ""); //Which Practioner, get arid of the line break

        if (PractionerID != CurrentPractionerID || i==lines.length-1) {//New Group when its a new practioner

            if (CurrentPractionerID != "") {
                memberPatient = memberPatient.trimRight();
                memberPatient = "[" + memberPatient + "]";
                memberPatient = JSON.parse(memberPatient)
                GroupResource = templateBundleResource(CurrentPractionerID, memberPatient); //write the last group to disk
                //GroupResource = JSON.parse(GroupResource);
                var GroupJSON = JSON.stringify(GroupResource, undefined, 4);
                //console.log(GroupJSON)
                fs.writeFile("Users/richardbraman/IdeaProjects/dpc-app/src/main/resources/groups/Group_Resource_for_NPI_" + CurrentPractionerID + ".json",
                    GroupJSON, function (err) {
                    if (err) return console.log(err);
                });
                GroupCount ++;
            }
            GroupId = uuidv4(); // ⇨ '9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d';
            CurrentPractionerID = PractionerID; //Set new pract id
            memberPatient = ""; //clear the member variable

        }

        PatientId = currentline[0];
         var newPatient = addMember(await lookupPatientIDfromMBI(PatientId));
        if (newPatient == "") memberPatient = memberPatient
        else
            if (memberPatient != "") memberPatient = memberPatient + "," + newPatient
            else memberPatient = newPatient
    }
    return "Success"
}

async function lookupPatientIDfromMBI(PatientId){


        if (access_Token=="") {
            try{
                const response =  await getAccessToken();
                access_Token =response;
                console.log(response.access_token);
            }
            catch(error){
                console.log("Error getting token")
            }
        }

    try {


        // the await eliminates the need for .then
        var url = "https://sandbox.dpc.cms.gov/api/v1/Patient?identifier=" + PatientId
        const res = await fetch(url, {
            method: 'get',
            headers: {
                "Accept": "application/fhir+json",
                "Authorization": `Bearer W3sidiI6MiwibCI6Imh0dHBzOi8vc2FuZGJveC5kcGMuY21zLmdvdi9hcGkiLCJpIjoiODk5NzBiMDQtMGI4Ni00NGY4LTkzNTUtOWQzMTM2YTFlMmZjIiwiYyI6W3siaTY0IjoiWkhCalgyMWhZMkZ5YjI5dVgzWmxjbk5wYjI0Z1BTQXkifSx7Imk2NCI6IlpYaHdhWEpsY3lBOUlESXdNakV0TURVdE1qZFVNakk2TkRRNk1EVXVNVE00T0RjMVdnIn0seyJpNjQiOiJiM0puWVc1cGVtRjBhVzl1WDJsa0lEMGdNelV4Wm1KaU5XWXRaakptT1MwME1EazBMV0pqTm1ZdE1tSXpOakF3WW1JMU5tVTUifSx7ImwiOiJsb2NhbCIsImk2NCI6IkFxQ0JhSHFnZ1doNmV0cjJmRnRhalg2RWNNaUktUGUydkNGaWNNZjltal9Wcnp0OEJTS1RCRVhzemtLcU11R1lIOEVzNmhjejVleWxOamVzdGhzSHJ2U0NGRnJwTDMwNGpnU2w2YWVZay0xbzJtZDBFWl9SeE1pQkY3S2ZLVHd3S1gxaUdYcjNoRHdCRzJ6UXlDeEJGWnZOdGU2NGFoNGd1aFdkNFVyVm5GV1d4SjZmNkNBZWZsOTBMTTdoc1NxYWxVLUFBM2hnWVhla01lNURWZyIsInY2NCI6IlNaeTI3cm1VeFVJdjlRMXpwZ2FiLU5SWmlIYTRtd09BbWNSaEo5Zkh0bEVfeU5vSXF4V2sxbGlHdlhOZW91QW9FX3N2d2JIcE1DXzhkRExfSFd4TmdBYWNCdlAtSGJsMCJ9LHsiaTY0IjoiWlhod2FYSmxjeUE5SURJd01qQXRNRFl0TVRoVU1EWTZNRE02TXpndU16UXpPRFk1V2cifV0sInM2NCI6Ijh0VDFsakJIY1VHOV9Ib3A5SEh5STNvZ0t3Y2xYRnJRRFI1enkwOXNFQWcifSx7InYiOjIsImkiOiJcdTAwMDLCoMKBaHrCoMKBaHp6w5rDtnxbWsKNfsKEcMOIwojDuMO3wrbCvCFicMOHw73Cmj_DlcKvO3xcdTAwMDVcIsKTXHUwMDA0RcOsw45Cwqoyw6HCmFx1MDAxRsOBLMOqXHUwMDE3M8Olw6zCpTY3wqzCtlx1MDAxQlx1MDAwN8Kuw7TCglx1MDAxNFrDqS99OMKOXHUwMDA0wqXDqcKnwpjCk8OtaMOaZ3RcdTAwMTHCn8ORw4TDiMKBXHUwMDE3wrLCnyk8MCl9Ylx1MDAxOXrDt8KEPFx1MDAwMVx1MDAxQmzDkMOILEFcdTAwMTXCm8ONwrXDrsK4alx1MDAxRSDCulx1MDAxNcKdw6FKw5XCnFXClsOEwp7Cn8OoIFx1MDAxRX5fdCzDjsOhwrEqwprClU_CgFx1MDAwM3hgYXfCpDHDrkNWIiwiczY0IjoiRWZKWjBWS0FEN21ZUzZ1VUljRmxNU2xNN25DYnVDM3dvazFGQ0YzYktGRSJ9XQ==`
            }

        })
        // this code is resumed once the fetch Promise is resolved.
        // res now has a value.
        let responsedata = await res.json();
        if (responsedata.total == 0){
            return "unknown";
        }
        else{
            console.log(responsedata.entry[0].resource.id);
            return responsedata.entry[0].resource.id;

        }
    }
    catch(err) {
        // because the promise could error, it is advised to use
        // try/catch. With a Promise, you would .then(cb).catch(errHandler)
        // but async/await doesn't utilize callbacks.

        // perform error handling or you can bubble it up.
        throw err
    }


}


function templateBundleResource(PractionerId, GroupMembers){

    return Grouptemplate=
      {
            "resourceType": "Group",
            "type": "person",
            "actual": true,
            "characteristic": [
                {
                    "code": {
                        "coding": [
                            {
                                "code": "attributed-to"
                            }
                        ]
                    },
                    "valueCodeableConcept": {
                        "coding": [
                            {
                                "system": "http://hl7.org/fhir/sid/us-npi",
                                "code": (PractionerId)
                            }
                        ]
                    },
                    "exclude": false
                }
            ],
            "member": (GroupMembers)
      }

}


function addMember(PatientID ) {
    if (PatientID!="unknown") {
        return  "{\"entity\": {\"reference\": \"Patient/" + PatientID + "\"}}";
    }
    else{
        return "";
    }
}

async function getAccessToken() {

        var dt = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = (dt + Math.random() * 16) % 16 | 0;
            dt = Math.floor(dt / 16);
            return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });

        var data = {
            "iss": "client token (macaroon)",
            "sub": "client token (macaroon)",
            "aud": "https://sandbox.dpc.cms.gov/api/v1/Token/auth",
            "exp": Math.round(new Date().getTime() / 1000) + 300,
            "iat": Math.round(Date.now()),
            "jti": uuid,
        };


        var secret = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "--------------------------------------------" +
            "-----END RSA PRIVATE KEY-----\n"; //PRIVATE KEY


            const header = {
                'alg': 'RS384',
                'kid': 'keyid from DPC',
            }
            var sPayload = JSON.stringify(data);
            var sJWT = jsrsasign.jws.JWS.sign("RS384", header, sPayload, secret);
            //console.log(sJWT)

            let response = await fetch('https://sandbox.dpc.cms.gov/api/v1/Token/auth', {
            method: 'POST',
            header: 'ACCEPT: application/x-www-form-urlencoded',
            body: new URLSearchParams({
                scope: "system/*.*",
                grant_type: "client_credentials",
                client_assertion_type: "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                client_assertion: sJWT
            })
            })
            let responsedata = await response.json();
            //console.log(responsedata.access_token);
            return responsedata.access_token;
}