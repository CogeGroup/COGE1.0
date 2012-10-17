package controllers;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Cliente;
import models.Commessa;
import models.GiornateLavorative;
import models.Gruppo;
import models.RendicontoAttivita;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import play.mvc.Controller;
import play.mvc.With;
import play.vfs.VirtualFile;
import secure.SecureCOGE;
import secure.StatisticheService;
import utility.MyUtility;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJValueFormatter;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.DJBuilderException;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Rotation;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;

@With(SecureCOGE.class)
public class StatisticheController extends Controller {

	public static void index() {
		render();
	}

	private static void exportPDF(JasperPrint jp, String nome) {
		try {
			response.setHeader("Content-disposition", "attachment;filename=" + nome + ".pdf");
			JasperExportManager.exportReportToPdfStream(jp, response.out);
		} catch (JRException e) {
			e.printStackTrace();
		}
	}
	
	/*RISORSE TOTALI*/
	public static void showRisorseTotali() {
		render();
	}

	public static void statisticaHTMLRisorseTotali() {
		List<Map> resultSet = StatisticheService.prepareReportRisorseTotali();
		boolean result = true;
		VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
		Map reportParams = new HashMap();
		reportParams.put("subreport", StatisticheService.prepareReportSubreportRisorseTotali());
		reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/risorse.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR,Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
	
	public static void statisticaPDFRisorseTotali() {
		String nome = "reportRisorseTotali_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFRisorseTotali(), nome);
	}
	private static JasperPrint preparePDFRisorseTotali() {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportRisorseTotali();
			VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
			Map reportParams = new HashMap();
			reportParams.put("subreport", StatisticheService.prepareReportSubreportRisorseTotali());
			reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
			VirtualFile vf = VirtualFile.fromRelativePath("reports/risorse.jasper");
			jp = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
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
	
	public static void statisticaHTMLRisorse(Integer mese, Integer anno) {
		List<Map> resultSet = StatisticheService.prepareReportRisorse(anno,mese);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("MESE", mese);
		reportParams.put("ANNO", anno);
		reportParams.put("subreport", StatisticheService.prepareReportTipoLavoro(anno,mese));
		VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
		reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_risorse.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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

	public static void statisticaPDFRisorse(Integer mese, Integer anno) {
		String nome = "reportRisorse_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFRisorse(mese,anno), nome);
	}
	private static JasperPrint preparePDFRisorse(Integer mese, Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportRisorse(anno,mese);
			Map reportParams = new HashMap();
			reportParams.put("MESE", mese);
			reportParams.put("ANNO", anno);
			reportParams.put("subreport", StatisticheService.prepareReportTipoLavoro(anno,mese));
			VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
			reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_risorse.jasper");
			jp = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	/*CLIENTI*/
	public static void clienti() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showClienti(Integer anno) {
		render(anno);
	}

	public static void statisticaHTMLClienti(Integer anno) {
		List<Map> resultSet = StatisticheService.prepareReportClienti(anno);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);
		
		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
		reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
		reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
		reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
		reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
		reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
		reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
		reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
		reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
		reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
		reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
		reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
		reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
		
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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

	public static void statisticaPDFClienti(Integer anno) {
		String nome = "reportClienti_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFClienti(anno), nome);
	}
	private static JasperPrint preparePDFClienti(Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportClienti(anno);
			Map reportParams = new HashMap();
			reportParams.put("ANNO", anno);

			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
			reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
			reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
			reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
			reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
			reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
			reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
			reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
			reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
			reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
			reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
			reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
			reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
			
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jp = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	/*COMMESSE DIPENDENTI ANNO*/
	public static void commesseAnno() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseAnno(Integer anno) {
		render(anno);
	}

	public static void statisticaHTMLCommesseAnno(String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false).fetch();
		listaCommessa = moveToLastStraordinari(listaCommessa);
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
			AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Float.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30)).setHeaderStyle(styleNome).setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Float.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10)).setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI").setSubtitle(" ANNO: " + anno).setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15).setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
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
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME,"./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,"/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportCommesseNonFatturabiliDIPAnno_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseAnno(anno), nome);
	}
	private static JasperPrint preparePDFCommesseAnno(String anno) {
		JasperPrint jp = new JasperPrint();
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false).fetch();
		listaCommessa = moveToLastStraordinari(listaCommessa);
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
			AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Float.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30)).setHeaderStyle(styleNome).setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Float.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10)).setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI").setSubtitle(" ANNO: " + anno).setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15).setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
		DynamicReport dr = drb.build();
		Collection dummyCollection = new ArrayList();
		dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliAnno(anno);
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		Map param = new HashMap();
		try {
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr,new ClassicLayoutManager(), param);
			jp = JasperFillManager.fillReport(jr, param, ds);
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	/*COMMESSE*/
	public static void commesse() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesse(Integer mese, Integer anno) {
		render(mese, anno);
	}

	public static void statisticaHTMLCommesse(String mese, String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false).fetch();
		listaCommessa = moveToLastStraordinari(listaCommessa);
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
			AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
					.setTitle("Totale Giorni").setWidth(new Integer(30)).setHeaderStyle(styleNome).setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Float.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10)).setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		String subTitle = "MESE: " + mese + " ANNO: " + anno;
		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",Integer.parseInt(mese),Integer.parseInt(anno)).first();
		if(gl != null) {
			subTitle += " (" + gl.giorni + ")";
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI").setSubtitle(subTitle)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle).setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
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
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportCommesseNonFatturabiliDIP_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesse(mese,anno), nome);
	}
	private static JasperPrint preparePDFCommesse(String mese, String anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Commessa> listaCommessa = Commessa.find("byCalcoloRicavi", false).fetch();
			listaCommessa = moveToLastStraordinari(listaCommessa);
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
				AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30)).setHeaderStyle(styleNome).setStyle(styleNome).build();
				drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
				drb.addColumn(columnState2);
				for (Commessa c : listaCommessa) {
					AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Float.class.getName()).setWidth(200)
							.setTitle(c.descrizione).setWidth(new Integer(16)).setStyle(style).build();
					drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
					drb.addColumn(columnState);
				}
			} catch (ColumnBuilderException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			String subTitle = "MESE: " + mese + " ANNO: " + anno;
			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",Integer.parseInt(mese),Integer.parseInt(anno)).first();
			if(gl != null) {
				subTitle += " (" + gl.giorni + ")";
			}
			drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI").setSubtitle(subTitle)
					.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle).setDetailHeight(5).setMargins(10, 10, 10, 28)
					.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
			DynamicReport dr = drb.build();
			Collection dummyCollection = new ArrayList();
			dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabili(mese, anno);
			JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
			Map param = new HashMap();
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), param);
			jp = JasperFillManager.fillReport(jr, param, ds);
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	private static List<Commessa> moveToLastStraordinari(List<Commessa> lista){
		List<Commessa> commesse = new ArrayList<Commessa>();
		List<Commessa> straordinari = new ArrayList<Commessa>();
		for (Commessa c : lista){
			if(c.codice.equals("ST")){
				straordinari.add(c);
			} else if(c.codice.equals("SF")){
				straordinari.add(c);
			} else if(c.codice.equals("SN")){
				straordinari.add(c);
			} else {
				commesse.add(c);
			}
		}
		commesse.addAll(straordinari);
		return commesse;
	}
	/*COMMESSE COCOPRO MESE*/
	public static void commesseCollaboratoriMese() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseCollaboratoriMese(Integer mese, Integer anno) {
		render(mese, anno);
	}

	public static void statisticaHTMLCommesseCollaboratoriMese(String mese, String anno) {
        boolean result = true;
		JasperPrint jrprint;
		Gruppo gruppo = Gruppo.find("byCodice","CoCoPro").first();
		List<Commessa> listaCommessa = gruppo.commesse;
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
			AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
					.setTitle("Totale Giorni").setWidth(new Integer(10)).setHeaderStyle(styleNome).setStyle(styleNome).build();
//			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM,null,new DJValueFormatter() {
	            public String getClassName() {
	                return String.class.getName();
	             }
	             
	             public Object evaluate(Object value, Map fields, Map variables, Map parameters) {
	            	Double x = (Double) value;
	            	DecimalFormat df = new DecimalFormat(".##");
	                return df.format(x);
	             }
	          });
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(5)).setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		String subTitle = "MESE: " + mese + " ANNO: " + anno;
		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",Integer.parseInt(mese),Integer.parseInt(anno)).first();
		if(gl != null) {
			subTitle += " (" + gl.giorni + ")";
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI").setSubtitle(subTitle)
				.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle).setDetailHeight(15).setMargins(30, 20, 30, 15)
				.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
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
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportCommesseNonFatturabiliCCPMese_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseCollaboratoriMese(mese,anno), nome);
	}
	private static JasperPrint preparePDFCommesseCollaboratoriMese(String mese, String anno) {
		JasperPrint jp = new JasperPrint();
		try {
			Gruppo gruppo = Gruppo.find("byCodice","CoCoPro").first();
			List<Commessa> listaCommessa = gruppo.commesse;
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
				AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(10)).setHeaderStyle(styleNome).setStyle(styleNome).build();
//				drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
				drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM,null,new DJValueFormatter() {
		            public String getClassName() {
		                return String.class.getName();
		             }
		             
		             public Object evaluate(Object value, Map fields, Map variables, Map parameters) {
		            	Double x = (Double) value;
		            	DecimalFormat df = new DecimalFormat(".##");
		                return df.format(x);
		             }
		          });
				drb.addColumn(columnState2);
				for (Commessa c : listaCommessa) {
					AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Double.class.getName())
							.setTitle(c.descrizione).setWidth(new Integer(5)).setStyle(style).build();
					drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
					drb.addColumn(columnState);
				}
			} catch (ColumnBuilderException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			String subTitle = "MESE: " + mese + " ANNO: " + anno;
			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",Integer.parseInt(mese),Integer.parseInt(anno)).first();
			if(gl != null) {
				subTitle += " (" + gl.giorni + ")";
			}
			drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI").setSubtitle(subTitle)
					.setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle).setDetailHeight(15).setMargins(30, 20, 30, 15)
					.setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
			DynamicReport dr = drb.build();
			Collection dummyCollection = new ArrayList();
			dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliCollaboratori(mese, anno);
			JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
			Map param = new HashMap();
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), param);
			jp = JasperFillManager.fillReport(jr, param, ds);
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	
	/*COMMESSE COCOPRO ANNO*/
	public static void commesseCollaboratoriAnno() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseCollaboratoriAnno(Integer anno) {
		render(anno);
	}

	public static void statisticaHTMLCommesseCollaboratoriAnno(String anno) {
        boolean result = true;
		JasperPrint jrprint;
		Gruppo gruppo = Gruppo.find("byCodice","CoCoPro").first();
		List<Commessa> listaCommessa = gruppo.commesse;
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
			AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
					.setTitle("Totale Giorni").setWidth(new Integer(10)).setHeaderStyle(styleNome).setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(5)).setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI").setSubtitle(" ANNO: " + anno).setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15).setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
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
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportCommesseNonFatturabiliCCPAnno_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseCollaboratoriAnno(anno), nome);
	}
	private static JasperPrint preparePDFCommesseCollaboratoriAnno(String anno) {
		JasperPrint jp = new JasperPrint();
		try {
			Gruppo gruppo = Gruppo.find("byCodice","CoCoPro").first();
			List<Commessa> listaCommessa = gruppo.commesse;
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
				AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(10)).setHeaderStyle(styleNome).setStyle(styleNome).build();
				drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
				drb.addColumn(columnState2);
				for (Commessa c : listaCommessa) {
					AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Double.class.getName())
							.setTitle(c.descrizione).setWidth(new Integer(5)).setStyle(style).build();
					drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
					drb.addColumn(columnState);
				}
			} catch (ColumnBuilderException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			drb.setTitle("REPORT COMMESSE NON FATTURABILI COLLABORATORI").setSubtitle(" ANNO: " + anno).setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
					.setDetailHeight(15).setMargins(30, 20, 30, 15).setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
			DynamicReport dr = drb.build();
			Collection dummyCollection = new ArrayList();
			dummyCollection = RendicontoAttivita.statisticheCommesseNonFatturabiliCollaboratoriAnno(anno);
			JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
			Map param = new HashMap();
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), param);
			jp = JasperFillManager.fillReport(jr, param, ds);
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	/*COMMESSE FATTURABILI*/
	public static void commesseClienti() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseClienti(Integer anno) {
		render(anno);
	}

	public static void statisticaHTMLCommesseClienti(Integer anno) {
		List<Map> resultSet = StatisticheService.prepareReportCommesseClienti(anno);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);

		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
		reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
		reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
		reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
		reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
		reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
		reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
		reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
		reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
		reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
		reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
		reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
		reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
		
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesse_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportCommesseClienti_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseClienti(anno), nome);
	}
	private static JasperPrint preparePDFCommesseClienti(Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportCommesseClienti(anno);
			Map reportParams = new HashMap();
			reportParams.put("ANNO", anno);

			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
			reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
			reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
			reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
			reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
			reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
			reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
			reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
			reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
			reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
			reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
			reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
			reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
			
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesse_clienti.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jp = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}

	/*COMMESSE NON FATTURABILI*/
	public static void commesseNonFatturabili() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseNonFatturabili(Integer anno) {
		render(anno);
	}

	public static void statisticaHTMLCommesseNonFatturabili(Integer anno) {
		List<Map> resultSet = StatisticheService.prepareReportCommesseNonFatturabili(anno);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);

		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
		reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
		reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
		reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
		reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
		reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
		reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
		reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
		reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
		reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
		reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
		reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
		reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
		
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesse_non_fatturabili.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportGiorniStatisticheDIP_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseNonFatturabili(anno), nome);
	}
	private static JasperPrint preparePDFCommesseNonFatturabili(Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportCommesseNonFatturabili(anno);
			Map reportParams = new HashMap();
			reportParams.put("ANNO", anno);

			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
			reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
			reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
			reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
			reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
			reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
			reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
			reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
			reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
			reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
			reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
			reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
			reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
			
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesse_non_fatturabili.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jp = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	/*COMMESSE NON FATTURABILI COLLABORATORI ANNO*/
	public static void commesseNonFatturabiliCollaboratoriAnno() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseNonFatturabiliCollaboratoriAnno(Integer anno) {
		render(anno);
	}
	
	public static void statisticaHTMLCommesseNonFatturabiliCollaboratoriAnno(Integer anno) {
		List<Map> resultSet = StatisticheService.prepareReportCommesseNonFatturabiliCollaboratori(anno);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);

		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
		reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
		reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
		reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
		reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
		reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
		reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
		reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
		reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
		reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
		reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
		reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
		reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
		
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesse_non_fatturabili_collaboratori.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportGiorniStatisticheCCP_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseNonFatturabiliCollaboratoriAnno(anno), nome);
	}
	private static JasperPrint preparePDFCommesseNonFatturabiliCollaboratoriAnno(Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportCommesseNonFatturabiliCollaboratori(anno);
			Map reportParams = new HashMap();
			reportParams.put("ANNO", anno);

			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
			reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
			reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
			reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
			reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
			reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
			reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
			reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
			reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
			reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
			reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
			reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
			reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
			
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesse_non_fatturabili_collaboratori.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jp = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	/*COMMESSE COCOPRO DETTAGLIO ASSENZA RETRIBUITA*/
	public static void commesseDettaglioAssenzaRetribuita() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}

	public static void showCommesseDettaglioAssenzaRetribuita(Integer anno) {
		render(anno);
	}

	public static void statisticaHTMLCommesseDettaglioAssenzaRetribuita(String anno) {
        boolean result = true;
		JasperPrint jrprint;
		List<Commessa> listaCommessa = Commessa.find("byCalcoloCostiAndCalcoloRicavi", true,false).fetch();
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
			AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
					.setTitle("Totale Giorni").setWidth(new Integer(30)).setHeaderStyle(styleNome).setStyle(styleNome).build();
			drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
			drb.addColumn(columnState2);
			for (Commessa c : listaCommessa) {
				AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Double.class.getName())
						.setTitle(c.descrizione).setWidth(new Integer(10)).setStyle(style).build();
				drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
				drb.addColumn(columnState);
			}
		} catch (ColumnBuilderException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI").setSubtitle(" ANNO: " + anno).setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
				.setDetailHeight(15).setMargins(30, 20, 30, 15).setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
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
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportDettaglioAssenzaRetribuitaCCP_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseDettaglioAssenzaRetribuita(anno), nome);
	}
	private static JasperPrint preparePDFCommesseDettaglioAssenzaRetribuita(String anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Commessa> listaCommessa = Commessa.find("byCalcoloCostiAndCalcoloRicavi", true, false).fetch();
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
				AbstractColumn columnState2 = ColumnBuilder.getNew().setColumnProperty("totaleGiorni",Double.class.getName())
						.setTitle("Totale Giorni").setWidth(new Integer(30)).setHeaderStyle(styleNome).setStyle(styleNome).build();
				drb.addGlobalFooterVariable(columnState2, DJCalculation.SUM);
				drb.addColumn(columnState2);
				for (Commessa c : listaCommessa) {
					AbstractColumn columnState = ColumnBuilder.getNew().setColumnProperty(c.idCommessa.toString(),Double.class.getName())
							.setTitle(c.descrizione).setWidth(new Integer(15)).setStyle(style).build();
					drb.addGlobalFooterVariable(columnState, DJCalculation.SUM);
					drb.addColumn(columnState);
				}
			} catch (ColumnBuilderException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			drb.setTitle("REPORT COMMESSE NON FATTURABILI DIPENDENTI").setSubtitle(" ANNO: " + anno).setDefaultStyles(titleStyle, titleStyle, headerStyle, titleStyle)
					.setDetailHeight(15).setMargins(30, 20, 30, 15).setPrintBackgroundOnOddRows(true).setGrandTotalLegend("").setUseFullPageWidth(true);
			DynamicReport dr = drb.build();
			Collection dummyCollection = new ArrayList();
			dummyCollection = RendicontoAttivita.statisticheDettaglioAssenzaRetribuitaCollaboratori(anno);
			JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
			Map param = new HashMap();
			JasperReport jr = DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager(), param);
			jp = JasperFillManager.fillReport(jr, param, ds);
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
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
		List<Map> resultSet = StatisticheService.prepareReportPortafoglioOrdini(anno);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("ANNO", anno);

		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
		reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
		reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
		reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
		reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
		reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
		reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
		reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
		reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
		reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
		reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
		reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
		gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
		reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
		
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/portafoglio_ordini.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jrprint = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
		String nome = "reportCategoriaAttivita_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFPortafoglioOrdini(anno), nome);
	}
	private static JasperPrint preparePDFPortafoglioOrdini(Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportPortafoglioOrdini(anno);
			Map reportParams = new HashMap();
			reportParams.put("ANNO", anno);

			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",1,anno).first();
			reportParams.put("ggGennaio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",2,anno).first();
			reportParams.put("ggFebbraio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",3,anno).first();
			reportParams.put("ggMarzo", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",4,anno).first();
			reportParams.put("ggAprile", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",5,anno).first();
			reportParams.put("ggMaggio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",6,anno).first();
			reportParams.put("ggGiugno", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",7,anno).first();
			reportParams.put("ggLuglio", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",8,anno).first();
			reportParams.put("ggAgosto", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",9,anno).first();
			reportParams.put("ggSettembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",10,anno).first();
			reportParams.put("ggOttobre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",11,anno).first();
			reportParams.put("ggNovembre", gl != null ? ""+gl.giorni+"" : " ");
			gl = GiornateLavorative.find("byMeseAndAnno",12,anno).first();
			reportParams.put("ggDicembre", gl != null ? ""+gl.giorni+"" : " ");
			
			VirtualFile vf = VirtualFile.fromRelativePath("reports/portafoglio_ordini.jrxml");
			JasperReport jasperReport = JasperCompileManager.compileReport(vf.getRealFile().getAbsolutePath());
			jp = JasperFillManager.fillReport(jasperReport, reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}
	
	public static void commesseACorpo() {
		List<String> listaAnni = RendicontoAttivita.find("select distinct anno from RendicontoAttivita").fetch();
		render(listaAnni);
	}
	
	public static void showCommesseACorpo(Integer mese, Integer anno) {
		render(mese, anno);
	}
	
	public static void statisticaHTMLCommesseACorpo(Integer mese, Integer anno) {
		List<Map> resultSet = StatisticheService.prepareReportCommesseACorpo(anno,mese);
		boolean result = true;
		Map reportParams = new HashMap();
		reportParams.put("MESE", mese);
		reportParams.put("ANNO", anno);
		GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",mese,anno).first();
		if(gl != null) {
			reportParams.put("GIORNI", gl.giorni);
			System.out.println(gl.giorni);
		}
		VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
		reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
		JasperPrint jrprint;
		try {
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesseACorpo.jasper");
			jrprint = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, new JRBeanCollectionDataSource(resultSet));
			if (jrprint.getPages().size() != 0) {
				JRHtmlExporter exporter = new JRHtmlExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jrprint);
				exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, "./images/");
				exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "/images/");
				exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.out);
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
	
	public static void statisticaPDFCommesseACorpo(Integer mese, Integer anno) {
		String nome = "reportCommesseACorpo_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		exportPDF(preparePDFCommesseACorpo(mese, anno), nome);
	}
	private static JasperPrint preparePDFCommesseACorpo(Integer mese, Integer anno) {
		JasperPrint jp = new JasperPrint();
		try {
			List<Map> resultSet = StatisticheService.prepareReportCommesseACorpo(anno,mese);
			Map reportParams = new HashMap();
			reportParams.put("MESE", mese);
			reportParams.put("ANNO", anno);
			GiornateLavorative gl = GiornateLavorative.find("byMeseAndAnno",mese,anno).first();
			if(gl != null) {
				reportParams.put("GIORNI", gl.giorni);
				System.out.println(gl.giorni);
			}
			VirtualFile vf1 = VirtualFile.fromRelativePath("reports/");
			reportParams.put("SUBREPORT_DIR", vf1.getRealFile().getAbsolutePath());
			VirtualFile vf = VirtualFile.fromRelativePath("reports/statistiche_commesseACorpo.jasper");
			jp = JasperFillManager.fillReport(vf.getRealFile().getAbsolutePath(), reportParams, new JRBeanCollectionDataSource(resultSet));
		} catch (JRException e) {
			e.printStackTrace();
		}
		return jp;
	}

}
