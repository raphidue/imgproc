$(function() {
	$('#send').click(function(event) {
		event.preventDefault();
		var name = $('#inputName').val();
		var email = $('#inputEmail').val();
		var message = $('#inputMessage').val();
		//console.log("Name: " + name + "    Email: " + email + "    Message: " + message);

		var json = {
			"name": name,
			"email": email,
			"message": message
		};

		jQuery.post("app_contact_sendEmail.html", json, function(data) {
			//("Email sending status: " + data['status'])
			$('#myModal').modal();
		}, "json");


	});
});