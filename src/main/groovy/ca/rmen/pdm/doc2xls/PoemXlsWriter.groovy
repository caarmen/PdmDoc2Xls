package ca.rmen.pdm.doc2xls

import jxl.Workbook
import jxl.format.CellFormat
import jxl.write.Label
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


/**
 * Created by calvarez on 28/02/15.
 */
class PoemXlsWriter {
    static void write(String path, Poem[] poems) {

        File file = new File(path)
        WritableWorkbook workbook = Workbook.createWorkbook(file)
        WritableSheet sheet = workbook.createSheet("Poem", 0)
        sheet.insertRow(0)
        sheet.getSettings().setVerticalFreeze(1)
        int col
        for (col = 0; col < 8; col++)
            sheet.insertColumn(col)
        Label cell = new Label(0, 0, " ")
        CellFormat cellFormat = cell.getCellFormat()
        col = 0
        for (String columnName in ["id", "type", "pageId", "title", "precontent", "content", "date", "location"]) {
            sheet.addCell(new Label(col++, 0, columnName, cellFormat))
        }
        int row = 0
        for (Poem poem : poems) {
            sheet.insertRow(++row);
            col = 0;
            sheet.addCell(new Label(col++, row, poem.id, cellFormat));
            sheet.addCell(new Label(col++, row, poem.type.name(), cellFormat));
            sheet.addCell(new Label(col++, row, poem.pageId, cellFormat));
            sheet.addCell(new Label(col++, row, poem.title, cellFormat));
            sheet.addCell(new Label(col++, row, poem.precontent, cellFormat));
            sheet.addCell(new Label(col++, row, poem.content, cellFormat));
            sheet.addCell(new Label(col++, row, poem.date, cellFormat));
            sheet.addCell(new Label(col, row, poem.location, cellFormat));
        }

        workbook.write()
        workbook.close()
        println "Wrote file ${file.getAbsolutePath()}"
    }
}
