//This sends out sms to phone number in row
function sendSms(to, body) {
  var SID = "SID" //*************Place SID Here******************
  var token = "Token" //*************Place Token Here******************

  var messages_url = "https://api.twilio.com/2010-04-01/Accounts/" + SID + "/Messages.json"; 
  var payload = {
    "To": to, //This will be the Cell Number from Cell
    "Body" : body, //This will be your message from Cell 
    "From" : "Twilio Number"
  };

  var options = {
    "method" : "post",
    "payload" : payload
  };

  options.headers = { 
    "Authorization" : "Basic " + Utilities.base64Encode(SID + ":" + token) 
  };

  UrlFetchApp.fetch(messages_url, options); //sends out SMS to number for the row
}

//This checks the phone number to see if it is a landline or mobile number and returns results
function lookup(phoneNumber) {
    var SID = "SID" //*************Place SID Here******************
    var token = "Token" //*************Place Token Here******************

    var lookupUrl = "https://lookups.twilio.com/v1/PhoneNumbers/" + phoneNumber + "?Type=carrier"; //Phone number from cell to be checked
    var options = {
        "method" : "get"
    };
 
    options.headers = {    
        "Authorization" : "Basic " + Utilities.base64Encode(SID + ":" + token) 
    };
  
    var response = UrlFetchApp.fetch(lookupUrl, options); //gets info about phone number
    var data = JSON.parse(response); 
    Logger.log(data); 
    return data; //sends it back to the sendAll function to be used. 
}


//Main part of the code. This is where you will make changes to match your sheet. 
function sendAll() {
  var width = 7  //*************How wide is your cell (A-B is 2, A-G is 7 and so on)******************
  var startRow = 3; //**********The row to start on change the 2 if numbers start lower down**********
  var numbers = 3  //*************What Column are cell phone numbers in(A=0, B=1 and so on)******************
  var msgcell = 6 //*************What Column is your messages written in(A=0, B=1 and so on)******************
  var stscell = 6 //*************What Column is your status of the app printed in(A=1, B=2 and so on)******************


  var sheet = SpreadsheetApp.getActiveSheet();//script runs on the sheet you have opened
  var numRows = sheet.getLastRow() - 1; //Figures out the last row
  var dataRange = sheet.getRange(startRow, 1, numRows, width) 
  var cellData = dataRange.getValues();

for (var i in cellData) { 
        var spreadsheetRow = startRow + Number(i);
        var info = cellData[i];
        var phoneNumber =  info[numbers]
        sheet.getRange(spreadsheetRow, stscell).setValue("");
        if (phoneNumber != "") { 
            try { 
                data = lookup(phoneNumber);
                if (data['status'] == 404) { 
                    sheet.getRange(spreadsheetRow, stscell).setValue("not found"); //This tells the script where to write message status to change 
                } 
                else if (data['carrier']['type'] == "mobile") {
                    try {
                        response_data = sendSms(info[numbers], info[msgcell]);  // Row[A] is "To" (the [] is the column number), Row[B] is "Body" (the [] is the column number) (0=A) CHANGE
                        status = "sent";
                    } catch(err) {
                      Logger.log(err);
                      status = "error";
                      }
                    sheet.getRange(spreadsheetRow, stscell).setValue(status); //row, column (this is where to print out status) (0=A?) CHANGE
                  }  
                else
                  {
                  sheet.getRange(spreadsheetRow, stscell).setValue("Number is a " + data['carrier']['type']);
                  }
                }
             catch(err) {
                Logger.log(err);
                sheet.getRange(spreadsheetRow, stscell).setValue('Check Number');
            }
        }
    }
}

function myFunction() {
  sendAll();
}
