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

		jQuery.post("app_contact_test.html", json, function(data) {
			alert("Name: " + data['name'] + "\nEmail: " + data['email']
				+ "\nMessage: " + data['message']);
		}, "json");


	});
});