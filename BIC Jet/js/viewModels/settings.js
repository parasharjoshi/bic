/**
 * @license
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */

define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojswitcher', 'ojs/ojprogress', 'ojs/ojtable',
    'ojs/ojarraydataprovider', 'ojs/ojknockout'],
        function (oj, ko, $, app, client) {

            function settingsViewModel() {
                var self = this;
                self.isModuleEnabled = ko.observable();
                self.isAdmin = ko.observable(app.isAdmin());
                self.selectedTab = ko.observable("current-model");
                self.model = ko.observable();
                self.initializing = ko.observable();
                self.inprogress = ko.observable(undefined);
                self.progressValue = ko.observable();
                self.errorMessage = ko.observable();
                self.modelInfoTableDataProvider = ko.observable();

                self.connected = function () {
                    self.resetFlags();
                    if (self.isAdmin()) {
                        self.initializeModule();
                    } else {
                        self.errorMessage("This page is restricted to Admins only! Please login  with proper credentials!!!");
                        self.isModuleEnabled(true);
                    }
                };

                self.disconnected = function () {
                    // Implement if needed
                };

                self.transitionCompleted = function () {
                    // Implement if needed
                };

                self.resetFlags = function () {
                    self.modelInfoTableDataProvider(undefined);
                    self.isModuleEnabled(undefined);
                    self.isAdmin(app.isAdmin());
                    self.selectedTab("current-model");
                    self.model(undefined);
                    self.initializing(undefined);
                    self.inprogress(undefined);
                    self.progressValue(undefined);
                    self.errorMessage(undefined);
                };

                self.initializeModule = function () {
                    self.progressValue(-1);
                    self.initializing(true);
                    client.invokeGet(app.hostBasePath() + '/admin/modelinfo')
                            .done(function (data) {
                                console.log("done...");
                                if (data.id) {
                                    self.model(data);
                                    self.populateModelData();
                                } else {
                                    self.errorMessage("There is no model available at the moment. Please contact admin.");
                                }
                                self.initializing(false);
                                self.isModuleEnabled(true);
                            })
                            .fail(function (xhr, status, error) {
                                console.log("fail...");
                                self.errorMessage("Initialation failed. Please contact admin if problem persists.");
                                self.initializing(false);
                                self.isModuleEnabled(false);
                            });
                };

                self.autoInitModel = function () {
                    self.inprogress('YES');
                    document.getElementById('auto-init-button').disabled = true;
                    client.invokePost(app.hostBasePath() + '/downloadInitModel',
                            {userName: app.userLogin()})
                            .done(function (data) {
                                self.initializeModule();
                                self.inprogress('DONE');
                            })
                            .fail(function (xhr, status, error) {
                                document.getElementById('auto-init-button').disabled = false;
                                self.inprogress('FAIL');
                                self.errorMessage("Operation failed. Please contact admin if problem persists.");
                            });
                };

                self.populateModelData = function () {
                    //Populate Model data
                    var modelProviderDataArray = [
                        {modelAttr: 'Model id/version', modelValue: self.model().id},
                        {modelAttr: 'Uploaded', modelValue: new Date(self.model().createdDate)},
                        {modelAttr: 'Modified', modelValue: new Date(self.model().modificationDate)},
                        {modelAttr: 'Upload by', modelValue: self.model().userName},
                        {modelAttr: 'Model file name', modelValue: self.model().modelFileName},
                        {modelAttr: 'Label file name', modelValue: self.model().labelFileName},
                        {modelAttr: 'Inception Model', modelValue: self.model().autoDownloaded}
                    ];
                    for (var i in self.model().attributes){
                        modelProviderDataArray.push({modelAttr: i, modelValue: self.model().attributes[i]});
                    }
                    
                    self.modelInfoTableDataProvider(new oj.ArrayDataProvider(modelProviderDataArray));
                };
            }

            /*
             * Returns a constructor for the ViewModel so that the ViewModel is constructed
             * each time the view is displayed.  Return an instance of the ViewModel if
             * only one instance of the ViewModel is needed.
             */
            return new settingsViewModel();
        }
);
