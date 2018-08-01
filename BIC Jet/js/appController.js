/**
 * @license
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your application specific code will go here
 */
define(['ojs/ojcore', 'knockout', 'ojs/ojmodule-element-utils', 'ojs/ojmodule-element', 'ojs/ojrouter', 'ojs/ojknockout', 'ojs/ojarraytabledatasource',
    'ojs/ojoffcanvas'],
        function (oj, ko, moduleUtils) {
            function ControllerViewModel() {
                var self = this;
                self.hostBasePath = ko.observable("http://parasjos-lap:8080/bic/rest");

                // Media queries for repsonsive layouts
                var smQuery = oj.ResponsiveUtils.getFrameworkQuery(oj.ResponsiveUtils.FRAMEWORK_QUERY_KEY.SM_ONLY);
                self.smScreen = oj.ResponsiveKnockoutUtils.createMediaQueryObservable(smQuery);
                var mdQuery = oj.ResponsiveUtils.getFrameworkQuery(oj.ResponsiveUtils.FRAMEWORK_QUERY_KEY.MD_UP);
                self.mdScreen = oj.ResponsiveKnockoutUtils.createMediaQueryObservable(mdQuery);

                // Router setup
                self.router = oj.Router.rootInstance;
                self.router.configure({
                    'activity': {label: 'Dashboard'},
                    'home': {label: 'Home'},
                    'login': {label: 'Home', isDefault: true},
                    'recognize': {label: 'Recognize'},
                    'settings': {label: 'Customers'},
                    'about': {label: 'About'},
                    'modelInfo': {label: 'Model Info'},
                    'signup': {label: 'Signup'},
                });
                oj.Router.defaults['urlAdapter'] = new oj.Router.urlParamAdapter();

                self.moduleConfig = ko.observable({'view': [], 'viewModel': null});

                self.loadModule = function () {
                    ko.computed(function () {
                        var name = self.router.moduleConfig.name();
                        var viewPath = 'views/' + name + '.html';
                        var modelPath = 'viewModels/' + name;
                        var masterPromise = Promise.all([
                            moduleUtils.createView({'viewPath': viewPath}),
                            moduleUtils.createViewModel({'viewModelPath': modelPath})
                        ]);
                        masterPromise.then(
                                function (values) {
                                    self.moduleConfig({'view': values[0], 'viewModel': values[1]});
                                },
                                function (reason) {}
                        );
                    });
                };

                // Navigation setup
                var navData = [
                    {name: 'Home', id: 'home',
                        iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-home-icon-24', access: 'user'},
                    {name: 'Activity', id: 'activity',
                        iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-grid-icon-16'},
                    {name: 'Recognize', id: 'recognize',
                        iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-library-icon-24'},
                    {name: 'Model', id: 'modelInfo',
                        iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-catalog-icon-24'},
                    {name: 'Settings', id: 'settings',
                        iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-gear-icon-16'},
                    {name: 'About', id: 'about',
                        iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-info-icon-24'}
                ];
                self.navDataSource = new oj.ArrayTableDataSource(navData, {idAttribute: 'id', });
                
                self.prefs = function () {
                    console.log("Got call to prefs");
                };
                self.help = function () {
                    console.log("Got call to help");
                };
                self.about = function () {
                    console.log("Got call to about");
                    self.router.go('about');
                };
                self.signout = function () {
                    self.userLogin(null);
                    self.isAdmin(null);
                    self.routeToLogin();
                };
                
                self.routeToLogin = function () {
                    if(!self.userLogin() || self.isAdmin()===undefined || self.isAdmin()===null){
                        self.router.go('login');
                    }
                };

                // Drawer
                // Close offcanvas on medium and larger screens
                self.mdScreen.subscribe(function () {
                    oj.OffcanvasUtils.close(self.drawerParams);
                });
                self.drawerParams = {
                    displayMode: 'push',
                    selector: '#navDrawer',
                    content: '#pageContent'
                };
                // Called by navigation drawer toggle button and after selection of nav drawer item
                self.toggleDrawer = function () {
                    return oj.OffcanvasUtils.toggle(self.drawerParams);
                }
                // Add a close listener so we can move focus back to the toggle button when the drawer closes
                $("#navDrawer").on("ojclose", function () {
                    $('#drawerToggleButton').focus();
                });

                // Header
                // Application Name used in Branding Area
                self.appName = ko.observable("Basic Image Classifier");
                
                /*self.userLogin = ko.observable('parasjos');
                self.isAdmin = ko.observable(true);
                self.userFullName = ko.observable('Parashar Joshi');*/
                // User Info used in Global Navigation area
                self.userLogin = ko.observable(null);
                self.isAdmin = ko.observable(null);
                self.userFullName = ko.observable();

                // Footer
                function footerLink(name, id, linkTarget) {
                    this.name = name;
                    this.linkId = id;
                    this.linkTarget = linkTarget;
                }
                self.footerLinks = ko.observableArray([
                    new footerLink('About Oracle', 'aboutOracle', 'http://www.oracle.com/us/corporate/index.html#menu-about'),
                    new footerLink('Contact Us', 'contactUs', 'http://www.oracle.com/us/corporate/contact/index.html'),
                    new footerLink('Legal Notices', 'legalNotices', 'http://www.oracle.com/us/legal/index.html'),
                    new footerLink('Terms Of Use', 'termsOfUse', 'http://www.oracle.com/us/legal/terms/index.html'),
                    new footerLink('Your Privacy Rights', 'yourPrivacyRights', 'http://www.oracle.com/us/legal/privacy/index.html')
                ]);
                
                self.routeToLogin();
            }

            return new ControllerViewModel();
        }
);
