/**
 * @license
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your dashboard ViewModel code goes here
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojprogress', 'ojs/ojlistview'],
        function (oj, ko, $, app, client) {

            function ActivityViewModel() {
                var self = this;
                self.inLoadingState = ko.observable(false);
                self.progressValue = ko.observable(-1);
                self.activityAvailable = ko.observable();
                self.activityData = ko.observable();
                self.errorMessage = ko.observable();
                self.noActivity = ko.observable();

                self.connected = function () {
                    self.clearVars();
                    self.getActivity();
                };

                self.clearVars = function () {
                    self.inLoadingState(false);
                    self.progressValue(-1);
                    self.activityAvailable(undefined);
                    self.noActivity(undefined);
                };

                self.disconnected = function () {
                };

                self.transitionCompleted = function () {
                };
                self.getActivity = function () {
                    self.inLoadingState(true);
                    client.invokeGet(app.hostBasePath() + '/activity')
                            .done(function (data) {
                                self.processActivityData(data);
                            })
                            .fail(function (xhr, status, error) {
                                self.inLoadingState(false);
                                self.errorMessage("Initialization failed. Could not fecth activity.");
                            });
                };

                self.processActivityData = function (data) {
                    var options = {year: 'numeric', month: 'long', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric'};
                    options.timeZoneName = 'short';
                    var activityProviderDataArray = [];

                    data.forEach(
                            function (item, i) {
                                if (item.request!==null) {
                                    activityProviderDataArray.push({
                                        actId: item.id,
                                        fullName: item.requestor.firstName + ' ' + item.requestor.lastName,
                                        username: item.requestor.userName,
                                        email: item.requestor.emailId,
                                        recogObj: item.request.recognizedObject,
                                        created: new Date(item.request.created).toLocaleDateString("en-US", options),
                                        type: 'REQUEST',
                                        reqId: item.request.id,
                                        fileName: item.request.fileName,
                                        userVote: item.request.userVote === true ? 'Accurate' : item.request.userVote === false ? 'Inaccurate' : 'Unknown',
                                        probability: item.request.probability
                                    });
                                } else if (item.model!==null) {
                                    activityProviderDataArray.push({
                                        actId: item.id,
                                        fullName: item.requestor.firstName + ' ' + item.requestor.lastName,
                                        username: item.requestor.userName,
                                        email: item.requestor.emailId,
                                        created: new Date(item.model.createdDate).toLocaleDateString("en-US", options),
                                        type: 'ADMIN',
                                        modelId: item.model.id
                                    });
                                }
                            }
                    );
                    self.noActivity(activityProviderDataArray.length>0 ? false: true);
                    self.activityData(activityProviderDataArray);
                    self.activityAvailable(true);
                    self.inLoadingState(false);
                };
            }

            return new ActivityViewModel();
        }
);
