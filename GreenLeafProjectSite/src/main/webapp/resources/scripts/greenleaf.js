function showDialog(id) { 
	var box = document.createElement('div');
	box.style.position = 'absolute';
	box.style.top = '0px';
	box.style.left = '0px';
	box.style.backgroundColor = '#000';
	box.style.overflow = 'hidden';
	box.style.width = '100%';
	box.style.height = document.body.scrollHeight+"px";
	box.style.filter = 'alpha(opacity=50)';
	box.style.opacity = '0.75';
	box.style.zindex = '99';
	box.style.display = 'none';
	box.id = 'dialogBack';
	
	$('body').append(box);
	$('#dialogBack').click(function() {
		$('#'+id).fadeOut("slow");
		$('#dialogBack').fadeOut(200);
		$('body').remove($('#'+id));
		
	});

	$('#dialogBack').fadeIn(200);
	
	
	$('#'+id).fadeIn("slow");
}