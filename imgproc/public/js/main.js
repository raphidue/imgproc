/************************Processing Buttons Action*****************************/
$(function() {
	$("#showImg").click(function() {
		if($( "#showImg" ).hasClass( "active" )) {
			$( "#showImg").removeClass( "active" )
		} else {
			$( "#showImg" ).addClass( "active" );
		}
	});
	$("#glaett").click(function() {
		buttonClicked("#glaett");
	});
	$("#diff").click(function() {
		buttonClicked("#diff");
	});
	$("#min").click(function() {
		buttonClicked("#min");
	});
	$("#max").click(function() {
		buttonClicked("#max");
	});
	$("#median").click(function() {
		buttonClicked("#median");
	});
	$("#gewMedian").click(function() {
		buttonClicked("#gewMedian");
	});
	$("#morph").click(function() {
		buttonClicked("#morph");
	});
	$("#region").click(function() {
		buttonClicked("#region");
	});
	$("#harri").click(function() {
		buttonClicked("#harri");
	});
});

// Check active buttons
function buttonClicked(elementID) {
	$( ".proc-button" ).removeClass( "active" );
	if($( elementID ).hasClass( "active" )) {
		$( elementID ).removeClass( "active" )
	} else {
		$( elementID ).addClass( "active" );
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
		$("#img-content").html('');
		$("#img-content").html('<img src="/assets/images/ajax-loader.gif" alt="Uploading...."/>');
		// get File Name for Post to Image Controller
		var filename = $('#upfile').val();
		var ts = Math.round((new Date()).getTime() / 1000);
		$("#form_id").attr("action", "/processing/" + ts + ".jpg");
		$("#form_id").ajaxForm(
			{
				target: '#img-content'
			}).submit();
	});
}); 