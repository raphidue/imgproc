var global_ID = null;
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
			sendJson("POST", "/smoothing", JSON.stringify({id: global_ID}));
			
			// warten bis Filteroperation angewendet wurde
			setTimeout(function () { 
				showHistogram("GET", "smoothing/" + global_ID + ".jpg");		
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
				showHistogram("GET", "minimum/" + global_ID + ".jpg");		
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
				showHistogram("GET", "maximum/" + global_ID + ".jpg");		
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
				showHistogram("GET", "median/" + global_ID + ".jpg");		
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
	});
	$("#harri").click(function() {
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
		$("#form_id").attr("action", "/processing/" + ts + ".jpg");
		$("#form_id").ajaxForm(
			{
				target: '#img-content',
				success: function() {
					showHistogram("GET", "processing/" + ts+ ".jpg");
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
	
// sendet die filtermatrix und id des zu bearbeitenden bildes als JSON 
function sendJson(typ, path, data) {
	
	// r für row c für column
	var r1c1 = $('#r1c1').val();
	var r1c2 = $('#r1c2').val();
	var r1c3 = $('#r1c3').val();
	var r2c1 = $('#r2c1').val();
	var r2c2 = $('#r2c2').val();
	var r2c3 = $('#r2c3').val();
	var r3c1 = $('#r3c1').val();
	var r3c2 = $('#r3c2').val();
	var r3c3 = $('#r3c3').val();
	
	// zusammenfügen von array und id
	data = '[' + data + ', {"r1c1":' + r1c1 +'},{"r1c2":' + r1c2 + '}, {"r1c3":' +
			 r1c3 + '}, {"r2c1":' + r2c1 + '}, {"r2c2":' + r2c2 + '}, {"r2c3":' +
			  r2c3 + '}, {"r3c1":' + r3c1 + '}, {"r3c2":' + r3c2 + '}, {"r3c3":' + r3c3 + '}]';	
	console.log(data);
	$.ajax({
		type: typ,
		data: data,
		contentType: 'application/json',
		dataType: 'json',
		url: path
	});
}

function showHistogram(typ, path) {
	$("#histo-label").fadeIn();
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