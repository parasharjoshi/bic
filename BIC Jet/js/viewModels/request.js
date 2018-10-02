/**
 * @license
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your about ViewModel code goes here
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'commonUtil', 'ojs/ojfilepicker',
    'ojs/ojprogress', 'ojs/ojinputtext', 'ojs/ojlabel', 'ojs/ojtable', 'ojs/ojarraydataprovider'],
        function (oj, ko, $, app, client, util) {

            function RequestViewModel() {
                var self = this;
                // Below are a set of the ViewModel methods invoked by the oj-module component.
                // Please reference the oj-module jsDoc for additional information.

                self.isUserAdmin = ko.observable(app.isAdmin());
                self.objectName = ko.observable();
                self.fileName = ko.observable();
                self.fileSrc = ko.observable();
                self.file = ko.observable();
                self.errorMessage = ko.observable();
                self.acceptStr = ko.observable("image/*");
                self.uploaded = ko.observable(false);
                self.progressValue = ko.observable(-1);
                self.inprogress = ko.observable();
                self.objDescription = ko.observable();
                self.requestDataProvider = ko.observable();
                self.requestArray = undefined;
                self.fileName = ko.observable();
                self.fileSrc = ko.observable();

                /**
                 * Optional ViewModel method invoked after the View is inserted into the
                 * document DOM.  The application can put logic that requires the DOM being
                 * attached here. 
                 * This method might be called multiple times - after the View is created 
                 * and inserted into the DOM and after the View is reconnected 
                 * after being disconnected.
                 */
                self.connected = function () {
                    console.log("IsUserAdmin? " + self.isUserAdmin());
                    self.resetVars();
                    self.isUserAdmin(app.isAdmin());
                    if (self.isUserAdmin()) {
                        self.fecthRequests();
                    }
                };

                /**
                 * Optional ViewModel method invoked after the View is disconnected from the DOM.
                 */
                self.disconnected = function () {
                    // Implement if needed
                };

                /**
                 * Optional ViewModel method invoked after transition to the new View is complete.
                 * That includes any possible animation between the old and the new View.
                 */
                self.transitionCompleted = function () {

                };

                self.selectListener = function (event) {
                    self.clearFiles();
                    var files = event.detail.files;
                    self.fileName(null);
                    if (files.length === 1 && files[0].type.startsWith("image/")) {
                        self.file(files[0]);
                        self.fileName(files[0].name);
                        var reader = new FileReader();
                        reader.onload = (function () {
                            return function (e) {
                                self.fileSrc(e.target.result);
                            };
                        })(files[0]);
                        // Read in the image file as a data URL.
                        reader.readAsDataURL(files[0]);
                    } else {
                        self.errorMessage("Please choose an image file. The ones that have a file extn like .jpg or .png or .jpeg");
                    }
                };

                self.clearFiles = function () {
                    self.fileName(null);
                    self.file(null);
                    self.fileSrc(null);
                    self.errorMessage(null);
                };

                self.resetVars = function () {
                    self.objectName("");
                    self.fileName(null);
                    self.file(null);
                    self.fileSrc(null);
                    self.uploaded(false);
                    self.errorMessage(null);
                    self.inprogress("");
                    self.objDescription("");
                    self.requestArray = undefined;
                };

                self.acceptArr = ko.pureComputed(function () {
                    var accept = self.acceptStr();
                    return accept ? accept.split(",") : [];
                }, self);

                self.uploadRequest = function () {
                    self.inprogress("YES");
                    var reader = new FileReader();
                    reader.onloadend = function (e) {
                        var fileContent = util.contentToBase64(reader.result);
                        //var fileContent = reader.result;

                        //self.fileSrc2("data:" + self.file().type + ";base64," + fileContent);
                        //self.errorMessage("Please wait...!");
                        client.invokePost(app.hostBasePath() + '/trainrequest',
                                {objName: self.objectName(), comment: self.objDescription(), fileName: self.file().name, content: fileContent, userName: app.userLogin(), mimeType: self.file().type, size: self.file().size, })
                                .done(function (data) {
                                    if (data && data.id && data.id > 0) {
                                        self.inprogress("DONE");
                                    } else {
                                        self.inprogress("FAIL");
                                    }
                                })
                                .fail(function (xhr, status, error) {
                                    self.inprogress("FAIL");
                                });
                    };
                    reader.readAsArrayBuffer(self.file());
                    self.uploaded(true);
                };

                self.fecthRequests = function () {
                    self.inprogress("YES");
                    client.invokeGet(app.hostBasePath() + '/admin/trainreq')
                            .done(function (data) {
                                if (data.length > 0) {
                                    self.requestArray = data;
                                    self.populateTableData(data);
                                } else {
                                    self.inprogress("DONE");
                                }
                            })
                            .fail(function (xhr, status, error) {
                                self.inprogress("FAIL");
                            });
                };

                self.tableSelectionChanged = function (event) {
                    if (event.detail.value[0] && event.detail.value[0].startKey) {
                        self.getRequestById(event.detail.value[0].startKey.row);
                        if (self.file()) {
                            self.fileName(self.file().fileName);
                            self.fileSrc("data:"+self.file().mimeType+";base64,"+self.file().content);
                        } else {
                            self.fileName("");
                            self.fileSrc("");
                        }
                    } else {
                        self.fileName("");
                        self.fileSrc("");
                    }
                };

                self.populateTableData = function (data) {
                    self.requestDataProvider(new oj.ArrayDataProvider(data, {keyAttributes: 'id'}));
                    self.inprogress('DONE');
                };

                self.getRequestById = function (id) {
                    self.requestArray.forEach(
                            function (item, i, array) {
                                if (item.id == id) {
                                    self.file(item);
                                }
                            });
                };

                self.requestsColumnsArray = [{"headerText": "Id",
                        "field": "id"},
                    {"headerText": "User",
                        "field": "userName"},
                    {"headerText": "Object Name",
                        "field": "objName"},
                    {"headerText": "Description",
                        "field": "comment"}];

            }

            /*
             * Returns a constructor for the ViewModel so that the ViewModel is constructed
             * each time the view is displayed.  Return an instance of the ViewModel if
             * only one instance of the ViewModel is needed.
             */
            return new RequestViewModel();
        }
);
