$(function() {
	$('#send').click(function(event) {
		event.preventDefault();
		var name = $('#inputName').val();
		var email = $('#inputEmail').val();
		var message = $('#inputMessage').val();
		//console.log("Name: " + name + "    Email: " + email + "    Message: " + message);

		var atpos=email.indexOf("@");
		var dotpos=email.lastIndexOf(".");
		if (atpos<1 || dotpos<atpos+2 || dotpos+2>=email.length){
			//alert("Not a valid e-mail address");
			$('#errorMessageEmail').removeClass("hidden");
			$('#inputEmail').closest('div').addClass("has-error");
			return;
	  	} else {
	  		$('#errorMessageEmail').addClass("hidden");
	  		$('#inputEmail').closest('div').removeClass("has-error");
	  	}

	  	if (name == "")
	  		name = "Anonymous";

	  	if (message.length < 30){
	  		$('#errorMessageMsg').removeClass("hidden");
			$('#inputMessage').closest('div').addClass("has-error");
			return;
	  	} else {
	  		$('#errorMessageMsg').addClass("hidden");
	  		$('#inputMessage').closest('div').removeClass("has-error");
	  	}

		var json = {
			"name": name,
			"email": email,
			"message": message
		};

		jQuery.post("app_contact_sendEmail.html", json, function(data) {
			//("Email sending status: " + data['status'])
			$("#inputMessage").val("");
			$('#myModal').modal();
		}, "json");



	});
});