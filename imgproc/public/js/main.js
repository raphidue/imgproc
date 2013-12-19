var global_ID = null;
// Verbirgt die Matrix Input-Maske und blendet diese ein, ja nach Filter
$(function(){
	$("#config-filter").hover(function(){
		$("#matrix-content").fadeIn();
	});
	$("#glaett").hover(function(){
		/*
		// Glättungsfilter default werte
		$("#r1c1").val(0.075);
		$("#r1c2").val(0.125);
		$("#r1c3").val(0.075);
		$("#r2c1").val(0.125);
		$("#r2c2").val(0.200);
		$("#r2c3").val(0.125);
		$("#r3c1").val(0.075);
		$("#r3c2").val(0.125);
		$("#r3c3").val(0.075);
		*/
		$("#matrix-content").fadeIn();
	});
	$("#diff").hover(function(){
		/*
		// Differenzfilter default werte
		$("#r1c1").val(-1);
		$("#r1c2").val(-2);
		$("#r1c3").val(-1);
		$("#r2c1").val(-2);
		$("#r2c2").val(16);
		$("#r2c3").val(-2);
		$("#r3c1").val(-1);
		$("#r3c2").val(-2);
		$("#r3c3").val(-1);
		*/
		$("#matrix-content").fadeIn();
	});
	$("#min").hover(function(){
		$("#matrix-content").fadeOut();
	});
	$("#max").hover(function(){
		$("#matrix-content").fadeOut();
	});
	$("#median").hover(function(){
		$("#matrix-content").fadeOut();
	});
	$("#gewMedian").hover(function(){
		$("#matrix-content").fadeIn();
	});
	$("#region").hover(function(){
		$("#matrix-content").fadeOut();
	});
	$("#morph").hover(function(){
		$("#matrix-content").fadeIn();
	});
});
/************************Processing Buttons Action*****************************/
$(function() {
	$("#showImg").click(function() {
		if($( "#showImg" ).hasClass( "active" )) {
			$( "#showImg").removeClass( "active" );
			$( "#hist-content" ).fadeOut();
		} else {
			$( "#showImg" ).addClass( "active" );
			$( "#hist-content" ).fadeIn();
		}
	});
	$("#config-filter").click(function() {
	});
	$("#glaett").click(function() {
		
		if(checkIfImageIsUploaded()) {
			// ID an path: smoothing senden und Histogramm erstellen
			if(!sendJson("POST", "/smoothing", JSON.stringify({id: global_ID})))
			return;
			
			// warten bis Filteroperation angewendet wurde
			setTimeout(function () { 
				showHistogram("GET", "smoothing/" + global_ID + ".png");		
			}, 1000);
			
			// refreshing image after use filter
			refreshImage();	
		}
	});
	$("#diff").click(function() {
	});
	$("#min").click(function() {

		if(checkIfImageIsUploaded()) {
			// ID an path: smoothing senden und Histogramm erstellen
			//sendJson("POST", "/minimum", JSON.stringify({id: global_ID}));
			

			$.ajax({
				type: "POST",
				data: JSON.stringify({id: global_ID}),
				contentType: 'application/json',
				dataType: 'json',
				url: "/minimum"
			});
			// warten bis Filteroperation angewendet wurde
			setTimeout(function () { 
				showHistogram("GET", "minimum/" + global_ID + ".png");		
			}, 10000);
			
			// refreshing image after use filter
			refreshImage();
		}
	});
	$("#max").click(function() {
		
		if(checkIfImageIsUploaded()) {
			// ID an path: smoothing senden und Histogramm erstellen
			//sendJson("POST", "/minimum", JSON.stringify({id: global_ID}));

			$.ajax({
				type: "POST",
				data: JSON.stringify({id: global_ID}),
				contentType: 'application/json',
				dataType: 'json',
				url: "/maximum"
			});
			// warten bis Filteroperation angewendet wurde
			setTimeout(function () { 
				showHistogram("GET", "maximum/" + global_ID + ".png");		
			}, 10000);
			
			// refreshing image after use filter
			refreshImage();
		}
	});
	$("#median").click(function() {
		
		if(checkIfImageIsUploaded()) {
			// ID an path: smoothing senden und Histogramm erstellen
			sendJson("POST", "/median", JSON.stringify({id: global_ID}));
			
			// warten bis Filteroperation angewendet wurde
			setTimeout(function () { 
				showHistogram("GET", "median/" + global_ID + ".png");		
			}, 1000);
			
			// refreshing image after use filter
			refreshImage();
		}
	});
	$("#gewMedian").click(function() {
	});
	$("#morph").click(function() {
	});
	$("#region").click(function() {
		if(checkIfImageIsUploaded()) {
			// ID an path: smoothing senden und Histogramm erstellen
			sendJson("POST", "/region", JSON.stringify({id: global_ID}));
			
			// warten bis Filteroperation angewendet wurde
			setTimeout(function () { 
				showBinaryHistogram("GET", "region/" + global_ID + ".png");		
			}, 1000);
			
			// refreshing image after use filter
			refreshImage();
		}
	});
});

function checkIfImageIsUploaded(){
	if(global_ID == null) {
		// Wenn kein Bild hochgeladen wurde
		$("#img-content").html('<div class="alert alert-danger">Oh Snap! Upload a image first and then select the filter again.</div>');
		return false;
	} else {
		return true;
	}
}

// Check active buttons
function buttonClicked(elementID) {
	$( ".proc-button" ).removeClass( "active" );
	if($( elementID ).hasClass( "active" )) {
		$( elementID ).removeClass( "active" )
	} else {
		$( elementID ).addClass( "active" );
	}
}

// Reset button states
function buttonReset(elementID) {
	if($( elementID ).hasClass( "active" )) {
		$( elementID ).removeClass( "active" );
	}
}
/******************************************************************************/
	
// Check active docu links
$(function() {
	$(".bs-sidenav > li").click(function() {
		$( ".bs-sidenav > li").removeClass( "active" );
		$(this).addClass( "active" );
	});
});
	
// For Uploadfile button
function getFile(){
	document.getElementById("upfile").click();
}

// Function for Post File to Image Controller
$(function() {
	$('#upfile').on('change', function()
	{
		setTimeout(function () { 
			$("#filter-label").fadeIn();
			$("#matrix-content").fadeIn();	
		}, 1000);
		$("#img-content").html('');
		$("#img-content").html('<img id="ajax-loader" src="/assets/images/ajax-loader.gif" alt="Uploading...."/>');
		// get File Name for Post to Image Controller
		var filename = $('#upfile').val();
		var ts = Math.round((new Date()).getTime() / 1000);
		global_ID = ts;
		$("#form_id").attr("action", "/processing/" + ts + ".png");
		$("#form_id").ajaxForm(
			{
				target: '#img-content',
				success: function() {
					showHistogram("GET", "processing/" + ts+ ".png");
				}
			}).submit();
		});
	});
		
	function saveImage() {
		var hiddenIFrameID = 'hiddenDownloader',
		iframe = document.getElementById(hiddenIFrameID);
		if (iframe === null) {
			iframe = document.createElement('iframe');
			iframe.id = hiddenIFrameID;
			iframe.style.display = 'none';
			document.body.appendChild(iframe);
		}
		iframe.src = document.getElementById("uploadedImage").src;
	}
	
	function refreshImage() {
		window.setTimeout( 
			function(){
				var img = document.getElementById("uploadedImage");
				img.setAttribute('src', img.getAttribute('src') + "?ts=" + new Date().getTime());
			}, 1000);
		}

		function validateMatrix(field) {
			var x = parseFloat(field.value);
			console.log("x = " + x + " typeof x = " + typeof x);
			if (isNaN(x)) {
				console.log("Bad input: " + field.value);
				field.value = "";
				// TODO: add chaning the color of element here, not in getMatrix
			}
		}

		function getMatrix() {
			var matrix = new Array();
			var toReturn = true;
			// r für row c für column
			for (var i = 0; i < 9; i++){
				var r = Math.floor(i/3)+1;
				var c = (i%3)+1;
				var id = '#r' + r + 'c' + c;
				matrix[i] = $(id).val();
				if (matrix[i] == ""){
					$(id).closest('div').addClass("has-error");
					toReturn = false;
				} else {
					$(id).closest('div').removeClass("has-error");
				}
			}
			return toReturn == true ? matrix : null;
		}
	
		// sendet die filtermatrix und id des zu bearbeitenden bildes als JSON 
		function sendJson(typ, path, data) {
	
			var matrix = getMatrix();
			if (matrix == null){
				$('#matrix-error').removeClass("hidden");
				return false;
			} else {
				$('#matrix-error').addClass("hidden");
			}
	
			// zusammenfügen von array und id
			data = '[' + data + ', {"r1c1":' + matrix[0] +'},{"r1c2":' + matrix[1] + '}, {"r1c3":' +
			matrix[2] + '}, {"r2c1":' + matrix[3] + '}, {"r2c2":' + matrix[4] + '}, {"r2c3":' +
			matrix[5] + '}, {"r3c1":' + matrix[6] + '}, {"r3c2":' + matrix[7] + '}, {"r3c3":' + matrix[8] + '}]';	
			console.log(data);
			$.ajax({
				type: typ,
				data: data,
				contentType: 'application/json',
				dataType: 'json',
				url: path
			});
			return true;
		}

		function showHistogram(typ, path) {
			$("#hist-content").fadeIn();
			var data = new Array(256);
			var array = new Array(256);

			$.ajax({
				type: typ,
				url: path,
				dataType: "json"
			}).done(function(jsonData) {
				for(i in jsonData) {
					data[i] = jsonData[i];
					array[i] = "";
				}
				var barChartData = {
					labels : array,
					datasets : [
					{
						fillColor : "rgba(220,220,220,0.5)",
						strokeColor : "rgba(220,220,220,1)",
						data : data
					}
					]

				}
				var options = {
					//Boolean - If we show the scale above the chart data			
					scaleOverlay : true,
					//Boolean - If we want to override with a hard coded scale
					scaleOverride : false,
					//** Required if scaleOverride is true **
					//Number - The number of steps in a hard coded scale
					scaleSteps : null,
					//Number - The value jump in the hard coded scale
					scaleStepWidth : null,
					//Number - The scale starting value
					scaleStartValue : null,
					//String - Colour of the scale line	
					scaleLineColor : "rgba(0,0,0,.1)",
					//Number - Pixel width of the scale line	
					scaleLineWidth : 1,
					//Boolean - Whether to show labels on the scale	
					scaleShowLabels : false,
					//Interpolated JS string - can access value
					scaleLabel : "<%=value%>",
					//String - Scale label font declaration for the scale label
					scaleFontFamily : "'Arial'",
					//Number - Scale label font size in pixels	
					scaleFontSize : 10,
					//String - Scale label font weight style	
					scaleFontStyle : "normal",
					//String - Scale label font colour	
					scaleFontColor : "#666",	
					///Boolean - Whether grid lines are shown across the chart
					scaleShowGridLines : false,
					//String - Colour of the grid lines
					scaleGridLineColor : "rgba(0,0,0,.05)",
					//Number - Width of the grid lines
					scaleGridLineWidth : 1,	
					//Boolean - If there is a stroke on each bar	
					barShowStroke : true,
					//Number - Pixel width of the bar stroke	
					barStrokeWidth : 1,
					//Number - Spacing between each of the X value sets
					barValueSpacing : 1,
					//Number - Spacing between data sets within X values
					barDatasetSpacing : 1,
					//Boolean - Whether to animate the chart
					animation : true,
					//Number - Number of animation steps
					animationSteps : 60,
					//String - Animation easing effect
					animationEasing : "easeOutQuart",
					//Function - Fires when the animation is complete
					onAnimationComplete : null

				}
				var myLine = new Chart(document.getElementById("canvas").getContext("2d")).Bar(barChartData, options);

			});
		}
		function showBinaryHistogram(typ, path) {
			$("#hist-content").fadeIn();
			var data = new Array(2);
			var array = new Array(2);

			$.ajax({
				type: typ,
				url: path,
				dataType: "json"
			}).done(function(jsonData) {
				for(i in jsonData) {
					data[i] = jsonData[i];
					array[i] = "";
				}
				var barChartData = {
					labels : array,
					datasets : [
					{
						fillColor : "rgba(220,220,220,0.5)",
						strokeColor : "rgba(220,220,220,1)",
						data : data
					}
					]

				}
				var options = {
					//Boolean - If we show the scale above the chart data			
					scaleOverlay : true,
					//Boolean - If we want to override with a hard coded scale
					scaleOverride : false,
					//** Required if scaleOverride is true **
					//Number - The number of steps in a hard coded scale
					scaleSteps : null,
					//Number - The value jump in the hard coded scale
					scaleStepWidth : null,
					//Number - The scale starting value
					scaleStartValue : null,
					//String - Colour of the scale line	
					scaleLineColor : "rgba(0,0,0,.1)",
					//Number - Pixel width of the scale line	
					scaleLineWidth : 1,
					//Boolean - Whether to show labels on the scale	
					scaleShowLabels : false,
					//Interpolated JS string - can access value
					scaleLabel : "<%=value%>",
					//String - Scale label font declaration for the scale label
					scaleFontFamily : "'Arial'",
					//Number - Scale label font size in pixels	
					scaleFontSize : 10,
					//String - Scale label font weight style	
					scaleFontStyle : "normal",
					//String - Scale label font colour	
					scaleFontColor : "#666",	
					///Boolean - Whether grid lines are shown across the chart
					scaleShowGridLines : false,
					//String - Colour of the grid lines
					scaleGridLineColor : "rgba(0,0,0,.05)",
					//Number - Width of the grid lines
					scaleGridLineWidth : 1,	
					//Boolean - If there is a stroke on each bar	
					barShowStroke : true,
					//Number - Pixel width of the bar stroke	
					barStrokeWidth : 1,
					//Number - Spacing between each of the X value sets
					barValueSpacing : 1,
					//Number - Spacing between data sets within X values
					barDatasetSpacing : 1,
					//Boolean - Whether to animate the chart
					animation : true,
					//Number - Number of animation steps
					animationSteps : 60,
					//String - Animation easing effect
					animationEasing : "easeOutQuart",
					//Function - Fires when the animation is complete
					onAnimationComplete : null

				}
				var myLine = new Chart(document.getElementById("canvas").getContext("2d")).Bar(barChartData, options);

			});
		}