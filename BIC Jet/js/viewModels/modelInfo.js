/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * modelInfo module
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojswitcher', 'ojs/ojprogress', 'ojs/ojtable',
    'ojs/ojarraydataprovider', 'ojs/ojknockout']
        , function (oj, ko, $, app, client) {
            /**
             * The view model for the main content view template
             */
            function modelInfoContentViewModel() {
                var self = this;
                self.selectedTab = ko.observable("model-info");
                self.initializing = ko.observable();
                self.model = ko.observable();
                self.progressValue = ko.observable(-1);
                self.modelInfoTableDataProvider = ko.observable();
                self.licenseInfo = ko.observable();
                self.labelDataProvider = ko.observable();
                self.labelData = ko.observableArray();
                self.noOfObjects = ko.observable();
                self.filter = ko.observable();
                self.errorMessage = ko.observable();

                self.connected = function () {
                    self.clearVars();
                    self.initializeModelInfo();
                };

                self.clearVars = function () {
                    self.userFullName = app.userFullName();
                    self.initializing(undefined);
                    self.model(undefined);
                    self.modelInfoTableDataProvider(undefined);
                    self.licenseInfo(undefined);
                    self.labelDataProvider(undefined);
                    self.labelData(undefined);
                    self.noOfObjects(undefined);
                    self.filter(undefined);
                    this.errorMessage(undefined);
                };

                self.disconnected = function () {
                    // Implement if needed
                };

                self.transitionCompleted = function () {
                    // Implement if needed
                };

                self.initializeModelInfo = function () {
                    self.progressValue(-1);
                    self.initializing(true);
                    client.invokeGet(app.hostBasePath() + '/modelinfo')
                            .done(function (data) {
                                console.log("done...");
                                //self.parseModelInfo(data);
                                if (data.id) {
                                    self.model(data);
                                    self.populateModelData();
                                } else {
                                    self.errorMessage("There is no model available at the moment. Please contact admin.");
                                    self.initializing(false);
                                }
                            })
                            .fail(function (xhr, status, error) {
                                console.log("fail...");
                                self.errorMessage("Initialation failed. Could not load data. Please contact admin if problem persists.");
                                self.initializing(false);
                            });
                };
                self.populateModelData = function () {
                    //Populate Model data
                    var modelProviderDataArray = [
                        {modelAttr: 'Model id/version', modelValue: self.model().id},
                        {modelAttr: 'Uploaded', modelValue: new Date(self.model().createdDate)},
                        {modelAttr: 'Modified', modelValue: new Date(self.model().modificationDate)},
                        {modelAttr: 'Upload by', modelValue: self.model().userName},
                        {modelAttr: 'Description', modelValue: self.model().info}
                    ];
                    self.modelInfoTableDataProvider(new oj.ArrayDataProvider(modelProviderDataArray));
                    //self.modelInfoTableDataProvider(new oj.ArrayDataProvider(modelProviderDataArray, {keyAttributes: 'modelAttr'}));

                    //populate label information
                    var labelDate = [];
                    var arrayData = self.model().label.split("\n");
                    arrayData.sort(function (a, b) {
                        return a.toLowerCase().localeCompare(b.toLowerCase());
                    });
                    self.noOfObjects(arrayData.length)
                    for (var i = 0; i <= arrayData.length; i = i + 4) {
                        labelDate.push({
                            v1: arrayData[i] ? arrayData[i] : new String(" "),
                            v2: arrayData[i + 1] ? arrayData[i + 1] : new String(" "),
                            v3: arrayData[i + 2] ? arrayData[i + 2] : new String(" "),
                            v4: arrayData[i + 3] ? arrayData[i + 3] : new String(" ")});
                    }
                    self.labelDataProvider(new oj.ArrayDataProvider(labelDate));

                    //Load license information
                    self.licenseInfo(self.model().licenseInfo);

                    self.initializing(false);
                };
            }

            return modelInfoContentViewModel;
        });
