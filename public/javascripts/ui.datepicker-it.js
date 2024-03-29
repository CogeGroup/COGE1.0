/* Italian initialisation for the jQuery UI date picker plugin. */
/* Written by Apaella (apaella@gmail.com). */
jQuery(function(jQuery){
	jQuery.datepicker.regional['it'] = {
		closeText: 'Chiudi',
		prevText: '&#x3c;Prec',
		nextText: 'Succ&#x3e;',
		currentText: 'Oggi',
		monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
		'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
		monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
		'Lug','Ago','Set','Ott','Nov','Dic'],
		dayNames: ['Domenica','Lunedi','Martedi','Mercoledi','Giovedi','Venerdi','Sabato'],
		dayNamesShort: ['Dom','Lun','Mar','Mer','Gio','Ven','Sab'],
		dayNamesMin: ['Do','Lu','Ma','Me','Gio','Ve','Sa'],
		dateFormat: 'dd-mm-yy', firstDay: 1,
		isRTL: false};
	jQuery.datepicker.setDefaults($.datepicker.regional['it']);
});
