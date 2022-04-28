/*
 *  Copyright 2020-2022 CROZ d.o.o, the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package net.croz.nrich.excel.api.service;

import net.croz.nrich.excel.api.request.CreateExcelReportRequest;

/**
 * Creates and writes excel report to the provided OutputStream.
 */
public interface ExcelReportService {

    /**
     * Writes the excel report to  the provided OutputStream.
     *
     * @param request configuration options for excel report with data to be written
     */
    void createExcelReport(CreateExcelReportRequest request);

}
