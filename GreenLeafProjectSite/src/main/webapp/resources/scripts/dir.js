var map;

function newDate() { 
	document.getElementById("dateSelector:endDate").value = document.getElementById("datepicker").value;
	document.getElementById("dateSelector:update").click();
	
}

function refreshPage() { 
	$('#mapContainer').fadeTo(1000, .4);
	$('#dataContainer').fadeTo(1000, .4);
}


function initMap() { 
    var latlng = new google.maps.LatLng(40, -95);
    var myOptions = {
      zoom: 4,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.TERRAIN
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

    var georssLayer = new google.maps.KmlLayer('http://'+document.location.host+'/GreenLeafProjectSite/service/dir/maps/impactedRegions/'+document.getElementById('impactId').value, {suppressInfoWindows: false});

	georssLayer.setMap(map);
}
function plotReports() { 

	var geocoder = new google.maps.ClientGeocoder();
	
	$.get("../service/dir/maps/reportstories", function(xml) { 
		var bounds = new google.maps.LatLngBounds();
		
		$(xml).find("report").each(function() { 
			var info = '<div class=\"infoWindow\">';
			info += 'Title: ';
			info += $("title", this).text();
			info += '<br/>';
			info += 'Source: ';			
			info += $("source", this).text();
			info += '<br/>';
			info += 'Published on: ';				
			info += $("pubdate", this).text();	
			info += '<br/>';
			info += '<a href=\"';				
			info += $("url", this).text();	
			info += '\">- View the Story -</a>';
			info += '</div>';
			geocoder.getLatLng($("city", this).text(), function(point) { 
				if ( point ) { 
					bounds.extend(new google.maps.LatLng(point.lat(), point.lng(), true));
					if ( map.getBoundsZoomLevel(bounds) > 7 ) { 
						map.setZoom(7);
					} else {
						map.setZoom(map.getBoundsZoomLevel(bounds));
					}
					
					map.setCenter(bounds.getCenter());
					
					var marker = new GMarker(point);
					map.addOverlay(marker);
					GEvent.addListener(marker, "click", function() {
						marker.openInfoWindowHtml(info);
						
					});
				}
			});
		});
	});
	
}

