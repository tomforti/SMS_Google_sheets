//This sends out sms to phone number in row
function sendSms(to, body) {
  var SID = "SID" //*************Place SID Here******************
  var token = "Token" //*************Place Token Here******************

  var messages_url = "https://api.twilio.com/2010-04-01/Accounts/" + SID + "/Messages.json"; 
  var payload = {
    "To": to, 
    "Body" : body,  
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
unction sendAll() {
  var width = 11  //*************How wide is your cell (A-B is 2, A-G is 7 and so on)******************
  var startRow = 4; //**********The row to start on change the 2 if numbers start lower down**********
  var numbers = 4  //*************What Column are cell phone numbers in(A=0, B=1 and so on)******************
  var msgcell = 10 //*************What Column is your messages written in(A=0, B=1 and so on)******************
  var stscell = 10 //*************What Column is your status of the app printed in(A=1, B=2 and so on)******************
  var timecell = 8 //*************What Column is your timestamp of the app printed in(A=1, B=2 and so on)*****************
  var leftmsgcell = 6 //*************What Column is your Left Message of the app printed in(A=1, B=2 and so on)*****************


  var sheet = SpreadsheetApp.getActiveSheet();//script runs on the sheet you have opened
  var numRows = sheet.getLastRow() - 1; //Figures out the last row
  var dataRange = sheet.getRange(startRow, 1, numRows, width) 
  var cellData = dataRange.getValues();
  var datestamp = Utilities.formatDate(new Date(), "GMT-4:00", "EEE, MMM d, yyyy");  //*************Change your time zone here************
  sheet.getRange(2, 1).setValue("DATE: " + datestamp); //************Loacton on where to place the Date(row,column), A=1******************


for (var i in cellData) { 
        var spreadsheetRow = startRow + Number(i);
        var info = cellData[i];
        var phoneNumber =  info[numbers] 
        var timestamp = Utilities.formatDate(new Date(), "GMT-4:00", "h:mm a"); //*************Change your time zone here************
        sheet.getRange(spreadsheetRow, stscell).setValue("");
        if (phoneNumber != "") { 
            try { 
                data = lookup(phoneNumber);
                if (data['status'] == 404) { 
                    sheet.getRange(spreadsheetRow, stscell).setValue("not found");  
                } 
                else if (data['carrier']['type'] == "mobile") {
                    try {
                        response_data = sendSms(info[numbers], info[msgcell]);  
                        sheet.getRange(spreadsheetRow, stscell).setValue('Message Sent');
                        sheet.getRange(spreadsheetRow, leftmsgcell).setValue('Texted');
                        sheet.getRange(spreadsheetRow, timecell).setValue(timestamp);
                    } catch(err) {
                      Logger.log(err);
                      sheet.getRange(spreadsheetRow, stscell).setValue('Message NOT Sent');
                      }
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

function clearAll() {
  var startRow = 4; //**********The row to start on change the 2 if numbers start lower down**********
  var stscell = 10 //*************What Column is your status of the app printed in(A=1, B=2 and so on)******************
  var timecell = 8 //*************What Column is your timestamp of the app printed in(A=1, B=2 and so on)*****************
  var leftmsgcell = 6 //*************What Column is your Left Message of the app printed in(A=1, B=2 and so on)*****************

  var sheet = SpreadsheetApp.getActiveSheet();//script runs on the sheet you have opened
  var numRows = sheet.getLastRow() - 1; //Figures out the last row

  sheet.getRange(startRow, stscell, numRows).clearContent();
  sheet.getRange(startRow, timecell, numRows).clearContent();
  sheet.getRange(startRow, leftmsgcell, numRows).clearContent();
}
