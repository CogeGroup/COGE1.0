package controllers;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.tools.reflect.Sample;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.core.registration.ColumnsGroupVariablesRegistrationManager;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.DJBuilderException;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Rotation;
import ar.com.fdvs.dj.domain.constants.Stretching;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;

import models.Cliente;
import models.Commessa;
import models.RendicontoAttivita;
import models.Risorsa;
import models.TipoRapportoLavoro;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.fonts.FontFamily;
import play.db.DB;
import play.mvc.Controller;
import play.mvc.With;
import play.vfs.VirtualFile;
import secure.SecureCOGE;

@With(SecureCOGE.class)
public class StatisticheController extends Controller {

	public static void index() {
		render();
	}
	
	/*RISORSE*/

	public static void risorse() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	

	public static void showRisorse(Integer mese, Integer anno) {
		render(mese, anno);
	}
	

	public static void statisticaPDFRisorse(Integer mese, Integer anno) {
		DateMidnight dataInizio = new DateMidnight().withDayOfMonth(1)
				.withMonthOfYear(mese).withYear(anno);
		DateMidnight dataFine = new DateMidnight().withMonthOfYear(mese)
				.withYear(anno).dayOfMonth().withMaximumValue();
		Map reportParams = new HashMap();
		reportParams.put("MESE", mese);
		reportParams.put("ANNO", anno);
		reportParams.put("DATA_INIZIO", dataInizio.toDate());
		reportParams.put("DATA_FINE", dataFine.toDate());
		VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
		reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
		String dateStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		JasperPrint jrprint;

		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_risorse.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile()
					.getAbsolutePath(), reportParams, DB.getConnection());
			response.setHeader("Content-disposition",
					"attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}

	public static void statisticaHTMLRisorse(Integer mese, Integer anno) {
		boolean result = true;
		DateMidnight dataInizio = new DateMidnight().withDayOfMonth(1)
				.withMonthOfYear(mese).withYear(anno);
		DateMidnight dataFine = new DateMidnight().withMonthOfYear(mese)
				.withYear(anno).dayOfMonth().withMaximumValue();
		Map reportParams = new HashMap();
		reportParams.put("MESE", mese);
		reportParams.put("ANNO", anno);
		reportParams.put("DATA_INIZIO", dataInizio.toDate());
		reportParams.put("DATA_FINE", dataFine.toDate());
		VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
		reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_risorse.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, DB.getConnection());
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();

			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
		} catch (JRException e) {
			e.printStackTrace();
		}

	}

	
	/*CLIENTI*/

	public static void clienti() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showClienti(Integer anno) {
		render(anno);
	}

	public static void statisticaPDFClienti(Integer anno) {
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		String dateStr = new SimpleDateFormat("yyyyMMddHHmm")
				.format(new Date());
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			response.setHeader("Content-disposition",
					"attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}

	public static void statisticaHTMLClienti(Integer anno) {
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();
			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
		} catch (JRException e) {
			e.printStackTrace();
		}

	}

	
	/*COMMESSE DIPENDENTI ANNO*/
	public static void commesseAnno() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseAnno(Integer anno) {
		render(anno);

	}

	public static void statisticaHTMLCommesseAnno(String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		styleNome.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		style.setFont(new Font(5,Font._FONT_TIMES_NEW_ROMAN,true));
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 35,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI")
				.setSubtitle(" ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliAnno(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,"/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				
				exporter.exportReport();

			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
			
			
		} catch (JRException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void statisticaPDFCommesseAnno(String anno) {

		String dateStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		style.setFont(new Font(5,Font._FONT_TIMES_NEW_ROMAN,true));
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 70,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
					.setTitle("Totale Giorni").setWidth(new Integer(35))
					.setHeaderStyle(styleNome)
					.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder
						.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(16))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI")
				.setSubtitle(" ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(5).setMargins(10, 10, 10, 28)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliAnno(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,
					new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			response.setHeader("Content-disposition","attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);

		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	/*COMMESSE*/
	public static void commesse() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesse(Integer mese, Integer anno) {
		render(mese, anno);

	}

	public static void statisticaHTMLCommesse(String mese, String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 35,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI")
				.setSubtitle("MESE: " + mese + " ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabili(mese, anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();

			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
			
			
		} catch (JRException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void statisticaPDFCommesse(String mese, String anno) {

		String dateStr = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		styleNome.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		style.setFont(new Font(5,Font._FONT_TIMES_NEW_ROMAN,true));
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));	
		
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 60,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder
						.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName()).setWidth(200)
						.setTitle(c.descrizione).setWidth(new Integer(16))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
//			AbstractColumn columnStateTest = ColumnBuilder
//					.getNew()
//					.setColumnProperty("test",Double.class.getName()).setWidth(200)
//					.setTitle("stest").setWidth(new Integer(16))
//					.setStyle(style).build();
//			
//			drb.addColumn(columnStateTest);

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI")
				.setSubtitle("MESE: " + mese + " ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(5).setMargins(10, 10, 10, 28)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabili(mese, anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,
					new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			response.setHeader("Content-disposition","attachment;filename=report"+dateStr+".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);

		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	/*COMMESSE COCOPRO MESE*/
	public static void commesseCollaboratoriMese() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseCollaboratoriMese(Integer mese, Integer anno) {
		render(mese, anno);

	}

	public static void statisticaHTMLCommesseCollaboratoriMese(String mese, String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicaviAndFlagCoCoPro", false, true).fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 10,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(10))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(5))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI")
				.setSubtitle("MESE: " + mese + " ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliCollaboratori(mese, anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();

			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
			
			
		} catch (JRException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void statisticaPDFCommesseCollaboratoriMese(String mese, String anno) {

		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicaviAndFlagCoCoPro", false, true).fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(80);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		styleNome.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 10,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(10))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder
						.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(5))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI")
				.setSubtitle("MESE: " + mese + " ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliCollaboratori(mese, anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,
					new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			response.setHeader("Content-disposition",
					"attachment;filename=report.pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);

		} catch (JRException e) {
			e.printStackTrace();
		}

	}	
	
	
	/*COMMESSE COCOPRO ANNO*/
	public static void commesseCollaboratoriAnno() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseCollaboratoriAnno(Integer anno) {
		render(anno);

	}

	public static void statisticaHTMLCommesseCollaboratoriAnno(String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicaviAndFlagCoCoPro", false, true).fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 10,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(10))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(5))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI")
				.setSubtitle(" ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliCollaboratoriAnno(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();

			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
			
			
		} catch (JRException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void statisticaPDFCommesseCollaboratoriAnno(String anno) {

		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicaviAndFlagCoCoPro", false, true).fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(80);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		styleNome.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 10,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(10))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder
						.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(5))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI")
				.setSubtitle(" ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliCollaboratoriAnno(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,
					new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			response.setHeader("Content-disposition",
					"attachment;filename=report.pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);

		} catch (JRException e) {
			e.printStackTrace();
		}

	}	
	
	
	

	
	
	
	
	
/*COMMESSE FATTURABILI*/
	
	public static void commesseClienti() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseClienti(Integer anno) {
		render(anno);
	}
	
	public static void statisticaHTMLCommesseClienti(Integer anno) {
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_commesse_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();
			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	public static void statisticaPDFCommesseClienti(Integer anno) {
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		String dateStr = new SimpleDateFormat("yyyyMMddHHmm")
				.format(new Date());
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_commesse_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			response.setHeader("Content-disposition",
					"attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	


/*COMMESSE NON FATTURABILI*/

	
	public static void commesseNonFatturabili() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseNonFatturabili(Integer anno) {
		render(anno);
	}
	
	public static void statisticaHTMLCommesseNonFatturabili(Integer anno) {
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_commesse_non_fatturabili.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();
			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	public static void statisticaPDFCommesseNonFatturabili(Integer anno) {
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		String dateStr = new SimpleDateFormat("yyyyMMddHHmm")
				.format(new Date());
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_commesse_non_fatturabili.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			response.setHeader("Content-disposition",
					"attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
/*COMMESSE NON FATTURABILI COLLABORATORI ANNO*/
	
	public static void commesseNonFatturabiliCollaboratoriAnno() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseNonFatturabiliCollaboratoriAnno(Integer anno) {
		render(anno);
	}
	
	public static void statisticaHTMLCommesseNonFatturabiliCollaboratoriAnno(Integer anno) {
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_commesse_non_fatturabili_collaboratori.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();
			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	public static void statisticaPDFCommesseNonFatturabiliCollaboratoriAnno(Integer anno) {
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		String dateStr = new SimpleDateFormat("yyyyMMddHHmm")
				.format(new Date());
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/statistiche_commesse_non_fatturabili_collaboratori.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			response.setHeader("Content-disposition",
					"attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	
	
	
	/*TEST JASPER REPORT*/
	public static void showTest() {

		Map reportParams = new HashMap();
		reportParams.put("xx", "aa");
		JasperPrint jrprint;

		ArrayList<HashMap> list = new ArrayList<HashMap>();
		list = Cliente.statisticheClienti(2010);
		// HashMap m1 = new HashMap();
		// m1.put("nome", "nome1");
		// m1.put("cognome", "cognome1");
		//
		// HashMap m2 = new HashMap();
		// m2.put("nome", "nome2");
		// m2.put("cognome", "cognome2");
		//
		// list.add(m1);
		// list.add(m2);
		//

		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/reportTestClienti.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile()
					.getAbsolutePath(), reportParams,
					new JRBeanCollectionDataSource(list));
			response.setHeader("Content-disposition",
					"attachment;filename=report.pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	/*TEST DYNAMIC JASPER REPORT*/
	public static void test() throws ColumnBuilderException,
	ClassNotFoundException, DJBuilderException {
		String mese = "6";
		String anno = "2011";
		JasperPrint jrprint;
		
		List<Commessa> listaCommessa = Commessa.find("byFatturabile", false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		// headerStyle.setPadding(1000);
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		
		// Style titleStyle = new Style();
		// titleStyle.setBorder(Border.DOTTED);
		// Style subtitleStyleParent = new Style("subtitleParent");
		// subtitleStyleParent.setBackgroundColor(Color.CYAN);
		// subtitleStyleParent.setTransparency(Transparency.OPAQUE);
		// subtitleStyleParent.setBorder(Border.PEN_4_POINT);
		// Style subtitleStyle =
		// Style.createBlankStyle("subtitleStyle","subtitleParent");
		// subtitleStyle.setFont(Font.GEORGIA_SMALL_BOLD);
		//
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		drb.addColumn("nominativo", "risorsa", String.class.getName(), 30,
				styleNome, styleNome);
		drb.addColumn("Totale Ore", "totaleOre", Integer.class.getName(), 30,
				styleNome, styleNome);
		for (Commessa c : listaCommessa) {
		
			// drb.addColumn(c.descrizione, c.idCommessa.toString(),
			// BigDecimal.class.getName(),10,style);
			AbstractColumn columnState = ColumnBuilder
					.getNew()
					.setColumnProperty(c.idCommessa.toString(),
							BigDecimal.class.getName()).setTitle(c.descrizione)
					.setWidth(new Integer(10)).setStyle(style).build();
			drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
			drb.addColumn(columnState);
		
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI")
				.setSubtitle("MESE: " + mese + " ANNO: " + anno)
				.setDefaultStyles(null, null, headerStyle, null)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		
		DynamicReport dr = drb.build();
		
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabili(
				mese, anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,
					new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			response.setHeader("Content-disposition",
					"attachment;filename=report.pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		
		} catch (JRException e) {
			e.printStackTrace();
		}
	
	}

	/*COMMESSE COCOPRO DETTAGLIO ASSENZA RETRIBUITA*/
	public static void commesseDettaglioAssenzaRetribuita() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseDettaglioAssenzaRetribuita(Integer anno) {
		render(anno);

	}

	public static void statisticaHTMLCommesseDettaglioAssenzaRetribuita(String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloCostiAndCalcoloRicavi", true,false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 35,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI")
				.setSubtitle(" ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheDettaglioAssenzaRetribuitaCollaboratori(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();

			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
			
			
		} catch (JRException e) {
			e.printStackTrace();
		}
		
		
	}

	public static void statisticaPDFCommesseDettaglioAssenzaRetribuita(String anno) {

		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloCostiAndCalcoloRicavi", true, false)
				.fetch();
		FastReportBuilder drb = new FastReportBuilder();
		Style headerStyle = new Style();
		headerStyle.setBorder(Border.PEN_1_POINT);
		headerStyle.setBorderColor(Color.black);
		headerStyle.setVerticalAlign(VerticalAlign.JUSTIFIED);
		headerStyle.setFont(Font.TIMES_NEW_ROMAN_SMALL);
		drb.setHeaderHeight(100);
		drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
		headerStyle.setRotation(Rotation.LEFT);
		headerStyle.setTransparency(Transparency.OPAQUE);
		Style styleNome = new Style();
		styleNome.setRotation(Rotation.NONE);
		styleNome.setBorder(Border.PEN_1_POINT);
		Style style = new Style();
		style.setBorder(Border.PEN_1_POINT);
		Style titleStyle = new Style();
		titleStyle.setFont(new Font(8,Font._FONT_TIMES_NEW_ROMAN,true));
		try {
			drb.addColumn("nominativo", "risorsa", String.class.getName(), 45,styleNome, styleNome);
			//drb.addColumn("Totale Giorni", "totaleGiorni", Double.class.getName(),30, styleNome, styleNome);
			AbstractColumn columnState2 = ColumnBuilder
					.getNew()
					.setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30))
						.setHeaderStyle(styleNome)
						.setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			
			
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder
						.getNew()
						.setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(15))
						.setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}

		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI")
				.setSubtitle(" ANNO: " + anno)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("")
				.setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheDettaglioAssenzaRetribuitaCollaboratori(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,
					new ClassicLayoutManager(), param);
			jrprint = JasperFillManager.fillReport(jr, param, ds);
			response.setHeader("Content-disposition",
					"attachment;filename=report.pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);

		} catch (JRException e) {
			e.printStackTrace();
		}

	}
/*PORTAFOGLIO ORDINI*/
	
	public static void portafoglioOrdini() {

		List<String> listaAnni = RendicontoAttivita.find(
				"select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showPortafoglioOrdini(Integer anno) {
		render(anno);
	}
	
	public static void statisticaHTMLPortafoglioOrdini(Integer anno) {
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/portafoglio_ordini.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(
						JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,
						Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,
						"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
						"/images/");
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
						response.out);
				exporter.exportReport();
			} else {
				result = false;
				response.status = 404;
				render("StatisticheController/error.html", result);
			}
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	public static void statisticaPDFPortafoglioOrdini(Integer anno) {
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		JasperPrint jrprint;
		String dateStr = new SimpleDateFormat("yyyyMMddHHmm")
				.format(new Date());
		try {
			VirtualFile vf = VirtualFile
					.fromRelativePath("reports/portafoglio_ordini.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf
					.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams,
					DB.getConnection());
			response.setHeader("Content-disposition",
					"attachment;filename=report_" + dateStr + ".pdf");
			JasperExportManager.exportReportToPdfStream(jrprint, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}

	}
	
	

}
