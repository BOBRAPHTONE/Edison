var DevKey;
var AppKey;
var Galileo = require("galileo-io");
var board = new Galileo();
var PIR = -1;
var TOUCH = -1;
var TEMP = -1;
var POT = -1;

var gto = {};
gto["EXPIRY"] = 60;

GolgiLib.init();
GolgiNet.init();
GolgiEdison.ServiceInit();

function setCredentials(){
    console.log('Attempting to set credentials');

    process.argv.forEach(function(val, index, array){
        console.log(index + ':' + val);
        if(val == '-devKey'){
            DevKey = array[index+1];
            console.log('DevKey: [' + DevKey + ']');
        }
        if(val == '-appKey'){
            AppKey = array[index+1];
            console.log('AppKey: [' + AppKey + ']');
        }
    });
    GolgiNet.setCredentials(DevKey, AppKey, "edison-board");
}

GolgiEdison.GolgiEdisonSvc.registerDigitalWriteHandler(function (resultHandler,control){
    var pinMode;

    // set the pin mode
    pinMode = board.MODES.OUTPUT;
    board.pinMode(control.getPin(),pinMode);
    
    // write to the pin
    console.log("Writing to pin: " + control.getPin() + " with value: " + control.getValue());
    board.digitalWrite(control.getPin(),control.getValue());

    resultHandler.success();
});

setCredentials();

GolgiNet.register(function(err){
    if(err != undefined){
        console.log("Failed to register");
    }   
    else{
        console.log("Register OK");
    }   
});


board.on("ready",function(){
this.pinMode("A0", board.MODES.ANALOG);
this.pinMode("A1", board.MODES.ANALOG);

this.digitalRead(12,function(data){
    if(data != PIR){
	console.log('Attempting to send to mobile device');
	PIR = data;
	var digital = GolgiEdison.Digital();
	digital.setPin(12);
	digital.setValue(PIR);
	GolgiEdison.GolgiEdisonSvc.digitalRead({
		success : function(){
			console.log("Successfully sent PIR value");
		},
		failWithGolgiException : function(golgiException){
			//console.log("Failed to send PIR value: " + golgiException.getErrText());
		}},
		"mobile-address",
		gto,
		digital);
    }
});

this.digitalRead(2,function(data){
    if(data != TOUCH){
	console.log('Attempting to send to mobile device');
	TOUCH = data;
	var digital = GolgiEdison.Digital();
	digital.setPin(2);
	digital.setValue(TOUCH);
	GolgiEdison.GolgiEdisonSvc.digitalRead({
		success : function(){
			console.log("Successfully sent TOUCH value");
		},
		failWithGolgiException : function(golgiException){
			// console.log("Failed to send TOUCH value: " + golgiException.getErrText());
		}},
		"mobile-address",
		gto,
		digital);
    }
});

this.analogRead("A0",function(data){
    if(Math.abs(TEMP - data) > 50){
	console.log('Attempting to send temperature to mobile device');
	TEMP = data;
	var analog = GolgiEdison.Analog();
	analog.setPin(0);
	analog.setValue(TEMP);
	GolgiEdison.GolgiEdisonSvc.analogRead({
		success : function(){
			console.log("Successfully sent TEMP value");
		},
		failWithGolgiException : function(golgiException){
//			console.log("Failed to send TEMP value: " + golgiException.getErrText());
		}},
		"mobile-address",
		gto,
		analog);
    }
});

this.analogRead("A1",function(data){
    if(Math.abs(POT - data) > 10){
	console.log('Attempting to send potentiometer to mobile device');
	POT = data;
	var analog = GolgiEdison.Analog();
	analog.setPin(1);
	analog.setValue(POT);
	GolgiEdison.GolgiEdisonSvc.analogRead({
		success : function(){
			console.log("Successfully sent POT value");
		},
		failWithGolgiException : function(golgiException){
			// console.log("Failed to send POT value: " + golgiException.getErrText());
		}},
		"mobile-address",
		gto,
		analog);
    }
});
});
