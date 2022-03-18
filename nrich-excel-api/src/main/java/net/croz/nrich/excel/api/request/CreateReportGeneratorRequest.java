package net.croz.nrich.excel.api.request;

import lombok.Builder;
import lombok.Getter;
import net.croz.nrich.excel.api.model.ColumnDataFormat;
import net.croz.nrich.excel.api.model.TemplateVariable;

import java.io.OutputStream;
import java.util.List;

@Getter
@Builder
public class CreateReportGeneratorRequest {

    /**
     * OutputStream where report will be written to (keep in mind closing of it is users responsibility).
     */
    private final OutputStream outputStream;

    /**
     * Path to template (template is resolved from this path using Springs ResourceLoader)
     */
    private final String templatePath;

    /**
     * List of {@link TemplateVariable} instances that will be used to replace variables defined in template.
     */
    private final List<TemplateVariable> templateVariableList;

    /**
     * List of {@link ColumnDataFormat} instances that allow for overriding of data format for specific columns.
     */
    private final List<ColumnDataFormat> columnDataFormatList;

    /**
     * Row index from which data should be written to report (if for example template holds column headers in first couple of rows).
     */
    private final int firstRowIndex;

}
