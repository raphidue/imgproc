$(function() {
    var shiftWindow = function() { scrollBy(0, -75) };
    if (location.hash) shiftWindow();
    window.addEventListener("hashchange", shiftWindow);
});
/************************Processing Buttons Action*****************************/
$(function() {
	$("#showImg").click(function() {
        buttonClicked("#showImg");
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
    $( ".btn" ).removeClass( "active" );
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
        $( ".bs-sidenav > li").removeClass( "sidenav-active" );
        $(this).addClass( "sidenav-active" );
    });
});