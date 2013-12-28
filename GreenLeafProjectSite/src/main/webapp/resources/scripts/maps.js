var map;
var mapStr;
var dmOpacityStr = '0.65';
var dirOpacityStr = '0.65';
var dmLayer;
var tmpLayer; 
var dirLayer;

var showDm = false;
var showDir = false;
var counties = false;
var showLayerDialog = false;

var URL_DATE_FORMAT_STRING = "yyyymmdd";

function initMap() { 
	var now = new Date();
	mapStr = now.format(URL_DATE_FORMAT_STRING);
	
	resizeMap(83);
	window.onresize = function() { resizeMap(35); }
    var latlng = new google.maps.LatLng(40, -95);
    var myOptions = {
      zoom: 4,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.TERRAIN
    };
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

    google.maps.event.addListener(map, 'bounds_changed', function() {
    	// decide if the dir layer should be redrawn
    	if ( showDir && map.getZoom() > 8 ) {
    		counties = true;
    		loadDir();
    	} else if ( showDir && counties ) { // the spatial type was counties but should be states again
    		counties = false;
    		loadDir();
    	}
    });
    
    loadDm();
}

function resizeMap(offset) {
	$("#map_canvas").height(window.innerHeight-(($("#header").outerHeight() + $("#footer").outerHeight())+offset));
	$("#layerPopupBack").height(window.innerHeight-(($("#header").outerHeight() + $("#footer").outerHeight())+offset));
}

function updateDmOpacity(value) { 
	dmOpacityStr = value / 100;
	loadDm();
}

function updateDirOpacity(value) { 
	dirOpacityStr = value / 100;
	loadDir();
}

function loadDm() { 
	if ( !showDm && dmLayer ) {
		dmLayer.setMap(null);
		dmLayer = null;
	} else if ( showDm ) {
		url = 'http://'+document.location.host+'/GreenLeafProjectSite/service/maps/dm/'+mapStr+'.kmz?opacity='+dmOpacityStr+"&nocache="+new Date().getTime();
		tmpLayer = new google.maps.KmlLayer(url, {preserveViewport: true, suppressInfoWindows: false});
		tmpLayer.setMap(map);
	
		if ( dmLayer ) { dmLayer.setMap(null); }
		dmLayer = tmpLayer;
	}
}

function processNewDate() { 
	var date_obj = $("#datepicker").datepicker("getDate");
	mapStr = date_obj.format(URL_DATE_FORMAT_STRING);
	loadDir();
	loadDm(false);
}

function loadDir() { 
	if ( !showDir && dirLayer ) {
		dirLayer.setMap(null);
		dirLayer = null;
	} else if ( showDir ) {
		if ( showDm ) { dmLayer.setMap(null); } // remove the DM layer
		if ( dirLayer ) { dirLayer.setMap(null); } // remove the existing DIR layer
		
		var type = ( map.getZoom() > 8 ) ? "spatialType=US_COUNTY" : "spatialType=US_STATE";
			
		if ( type == "spatialType=US_COUNTY" ) {
			url = 'http://'+document.location.host+'/GreenLeafProjectSite/service/maps/dir/categories/'+mapStr+'.json?opacity=weeks=24&'+type;
			
			var bounds = map.getBounds();
			var ne = bounds.getNorthEast();
			var sw = bounds.getSouthWest();
			url += ("&north="+ne.lat()+"&east="+ne.lng()+"&south="+sw.lat()+"&west="+sw.lng());
			alert(url);
		} else {
			url = 'http://'+document.location.host+'/GreenLeafProjectSite/service/maps/dir/categories/'+mapStr+'.kmz?opacity='+dirOpacityStr+"&weeks=24&"+type;
			dirLayer = new google.maps.KmlLayer(url, {preserveViewport: true, suppressInfoWindows: false});
			dirLayer.setMap(map);
			if ( showDm ) { loadDm(); } // add it back so that it is on top
		}
	}
}


function toggleDir() { 
	showDir = !showDir;
	loadDir();
}

function toggleDm() { 
	showDm = !showDm;
	loadDm(true);
}

function toggleLayerDisplay() { 
	showLayerDialog = !showLayerDialog;
	
	if ( showLayerDialog ) {
	var options = {};
	$("#layerPopup").show();
	} else { 
		$("#layerPopup").hide();
	}
}