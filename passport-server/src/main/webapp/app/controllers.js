/**
 * Controllers
 */
angular
    .module('smartcloudserviceApp')
    .controller('MainController', MainController)
    .controller('LeftSidebarController', LeftSidebarController)
    .controller('ErrorPageController', ErrorPageController)
    .controller('LoginController', LoginController)
    .controller('NavbarController', NavbarController)
    .controller('FooterController', FooterController)
    .controller('ProfileController', ProfileController)
    .controller('RegisterController', RegisterController)
    .controller('ActivationController', ActivationController)
    .controller('ForgotPasswordController', ForgotPasswordController)
    .controller('ResetPasswordController', ResetPasswordController)
    .controller('PasswordController', PasswordController)
    .controller('MetricsController', MetricsController)
    .controller('MetricsDialogController', MetricsDialogController)
    .controller('HealthController', HealthController)
    .controller('HealthDialogController', HealthDialogController)
    .controller('ConfigurationController', ConfigurationController)
    .controller('BeansController', BeansController)
    .controller('MappingsController', MappingsController)
    .controller('HttpTraceController', HttpTraceController)
    .controller('HttpSessionController', HttpSessionController)
    .controller('AuditsController', AuditsController)
    .controller('TrackerController', TrackerController)
    .controller('DictListController', DictListController)
    .controller('DictDialogController', DictDialogController)
    .controller('DictItemListController', DictItemListController)
    .controller('DictItemDialogController', DictItemDialogController)
    .controller('LoggerController', LoggerController)
    .controller('ScheduleController', ScheduleController)
    .controller('ControlController', ControlController)
    .controller('AppListController', AppListController)
    .controller('AppDialogController', AppDialogController)
    .controller('AppDetailsController', AppDetailsController)
    .controller('AuthorityListController', AuthorityListController)
    .controller('AuthorityDialogController', AuthorityDialogController)
    .controller('OAuth2ClientListController', OAuth2ClientListController)
    .controller('OAuth2ClientDialogController', OAuth2ClientDialogController)
    .controller('OAuth2ClientDetailsController', OAuth2ClientDetailsController)
    .controller('OAuth2AccessTokenListController', OAuth2AccessTokenListController)
    .controller('OAuth2AccessTokenDetailsController', OAuth2AccessTokenDetailsController)
    .controller('OAuth2RefreshTokenListController', OAuth2RefreshTokenListController)
    .controller('OAuth2RefreshTokenDetailsController', OAuth2RefreshTokenDetailsController)
    .controller('OAuth2ApprovalListController', OAuth2ApprovalListController)
    .controller('OAuth2ApprovalDetailsController', OAuth2ApprovalDetailsController)
    .controller('AdminMenuListController', AdminMenuListController)
    .controller('AdminMenuDialogController', AdminMenuDialogController)
    .controller('AuthorityAdminMenuController', AuthorityAdminMenuController)
    .controller('UserListController', UserListController)
    .controller('UserDialogController', UserDialogController)
    .controller('UserDetailsController', UserDetailsController);

/**
 * MainController - controller
 * Contains several global data used in different view
 *
 */
function MainController($http, $rootScope, $scope, $state, AuthenticationService, PrincipalService, AuthorityAdminMenuService, AuthServerService, AlertUtils, APP_NAME, COMPANY_NAME) {
    var main = this;
    main.account = null;
    main.isAuthenticated = null;
    main.links = [];
    main.selectedLink = null;
    main.selectLink = selectLink;
    $rootScope.companyName = COMPANY_NAME;

    // Authenticate user whether has logged in
    AuthenticationService.authorize(false, getAccount);

    $scope.$on('authenticationSuccess', function () {
        getAccount();
    });

    $scope.$watch(PrincipalService.isAuthenticated, function () {
        loadLinks();
    });

    function loadLinks() {
        if (PrincipalService.isAuthenticated() == true) {
            main.links = AuthorityAdminMenuService.queryUserLinks({appName: APP_NAME});
        }
    }

    function getAccount() {
        PrincipalService.identity().then(function (account) {
            if (account == null) {
                return;
            }
            main.account = account;

            var authToken = AuthServerService.getToken();
            if (authToken) {
                main.account.profilePhotoUrl = '/api/account/profile-photo?access_token=' + authToken.access_token;
            }

            main.isAuthenticated = PrincipalService.isAuthenticated;

            if (account) {
                AlertUtils.success('登录成功');
            }
        });
    }

    function selectLink($item, $model, $label, $event) {
        $state.go(main.selectedLink.url);
    }
}
/**
 * LeftSidebarController
 */
function LeftSidebarController($scope, $state, $element, $timeout, APP_NAME, AuthorityAdminMenuService, PrincipalService) {
    var vm = this;

    vm.init = init;
    vm.groups = [];

    $scope.$watch(PrincipalService.isAuthenticated, function () {
        vm.init();
    });

    function init() {
        if (PrincipalService.isAuthenticated() == true) {
            AuthorityAdminMenuService.queryUserMenus({appName: APP_NAME}, function (response) {
                if (response) {
                    vm.groups = response;
                    // Call the metsiMenu plugin and plug it to sidebar navigation
                    $timeout(function () {
                        $element.metisMenu();
                    });
                }
            }, function (errorResponse) {
            });
        }
    }
}
/**
 * ErrorPageController
 */
function ErrorPageController($state, $stateParams, $scope, JSONFormatterConfig) {
    var vm = this;

    vm.errorMessage = $stateParams.errorMessage;
}
/**
 * LoginController
 */
function LoginController($rootScope, $state, AuthenticationService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.userName = null;
    vm.password = null;
    vm.errorMsg = null;
    vm.login = login;
    vm.requestResetPassword = requestResetPassword;
    vm.isSaving = false;

    function login(event) {
        event.preventDefault();
        vm.isSaving = true;
        AuthenticationService.login({
                userName: vm.userName,
                password: vm.password,
                rememberMe: vm.rememberMe
            },
            function (data) {
                vm.errorMsg = null;
//            if ($state.current.name === 'register' || $state.current.name === 'activate' ||
//                $state.current.name === 'finishReset' || $state.current.name === 'requestReset') {
//                $state.go('home');
//            }

                $rootScope.$broadcast('authenticationSuccess');

                // previousState was set in the authExpiredInterceptor before being redirected to login modal.
                // since login is successful, go to stored previousState and clear previousState
                if (AuthenticationService.getPreviousState()) {
                    var previousState = AuthenticationService.getPreviousState();
                    AuthenticationService.resetPreviousState();
                    $state.go(previousState.name, previousState.params);
                }

                $state.go('dashboard');
            },
            function (data) {
                vm.errorMsg = data.error_description;
                vm.isSaving = false;
            });
    }

    function requestResetPassword() {
        $state.go('requestReset');
    }
}
/**
 * NavbarController
 */
function NavbarController($rootScope, $scope, $translate, $state, AuthenticationService, PrincipalService, ProfileService) {
    var vm = this;

    vm.isNavbarCollapsed = true;
    vm.isAuthenticated = PrincipalService.isAuthenticated;
    vm.changeLanguage = changeLanguage;

    ProfileService.getProfileInfo().then(function (response) {
        vm.inProduction = response.inProduction;
        vm.swaggerEnabled = response.swaggerEnabled;
    });

    vm.logout = logout;
    vm.toggleNavbar = toggleNavbar;
    vm.collapseNavbar = collapseNavbar;
    vm.$state = $state;

    $rootScope.isNavbarLoaded = true;

    function changeLanguage(langKey) {
        $translate.use(langKey);
        $scope.language = langKey;
    }
    function logout() {
        AuthenticationService.logout();
        $state.go('login');
    }

    function toggleNavbar() {
        vm.isNavbarCollapsed = !vm.isNavbarCollapsed;
    }

    function collapseNavbar() {
        vm.isNavbarCollapsed = true;
    }
}

/**
 * FooterController
 */
function FooterController($http, PrincipalService) {
    var vm = this;

    PrincipalService.hasAuthority('ROLE_DEVELOPER')
        .then(function (result) {
            if (result) {
                $http({
                    url: 'api/system/internet-ip',
                    method: 'GET',
                    transformResponse: [function (data) {
                        return data;
                    }]
                }).then(function (response) {
                    vm.ip = response.data;
                });
            }
        });
}
/**
 * ProfileController
 */
function ProfileController($state, PrincipalService, AccountService, AuthServerService, Upload) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.save = save;
    vm.isSaving = false;
    vm.profileAccount = null;
    vm.file = null;
    vm.uploading = false;
    vm.uploadProgress = 0;
    vm.upload = upload;

    var authToken = AuthServerService.getToken();
    if (authToken) {
        vm.profilePhotoUrl = '/api/account/profile-photo?access_token=' + authToken.access_token;
    }

    /**
     * Store the 'profile account' in a separate variable, and not in the shared 'account' variable.
     */
    var copyAccount = function (account) {
        return {
            activated: account.activated,
            email: account.email,
            mobileNo: parseInt(account.mobileNo),
            firstName: account.firstName,
            lastName: account.lastName,
            userName: account.userName,
            hasProfilePhoto: account.hasProfilePhoto
        };
    };

    PrincipalService.identity().then(function (account) {
        vm.profileAccount = copyAccount(account);
    });

    function save() {
        vm.isSaving = true;
        AccountService.update(vm.profileAccount,
            function (response) {
                vm.isSaving = false;
                PrincipalService.identity(true).then(function (account) {
                    vm.profileAccount = copyAccount(account);
                });
            },
            function (response) {
                vm.isSaving = false;
            });
    }

    function upload(file) {
        if (file) {
            vm.uploading = true;
            vm.uploadProgress = 30;
            Upload.upload({
                url: '/api/account/profile-photo/upload',
                // the 'file' must match the parameter of backend
                data: {file: file, description: "user profile"},
                disableProgress: false
            }).then(function (resp) {
                vm.uploadProgress = 100;
                vm.uploading = false;
            }, function (resp) {
                // if (resp.status > 0)
                //     vm.errorMsg = resp.status + ': ' + resp.data;
                vm.uploading = false;
            }, function (evt) {
                // does not work
                vm.uploadProgress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                vm.uploading = false;
            });
        }
    }
}
/**
 * RegisterController
 */
function RegisterController($state, $timeout, AuthenticationService, RegisterService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.register = register;
    vm.isSaving = false;
    vm.registerAccount = {};
    vm.passwordNotMatch = false;

    $timeout(function () {
        angular.element('#userName').focus();
    });

    function register() {
        if (vm.registerAccount.password !== vm.confirmPassword) {
            vm.passwordNotMatch = true;
        } else {
            vm.isSaving = true;
            RegisterService.create(vm.registerAccount,
                function (account) {
                    vm.isSaving = false;
                    vm.passwordNotMatch = false;
                    $state.go('login');
                },
                function (response) {
                    AuthenticationService.logout();
                    vm.isSaving = false;
                    vm.passwordNotMatch = false;
                });
        }
    }
}
/**
 * ActivationController
 */
function ActivationController($state, $stateParams, ActivateService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.success = false;
    vm.errorMessage = null;

    if ($stateParams.key) {
        ActivateService.get({key: $stateParams.key},
            function (response) {
                vm.success = true;
                vm.fieldErrors = [];
            },
            function (response, headers) {
                vm.success = false;
                vm.errorMessage = response.data.message;
            });
    }
}
/**
 * ForgotPasswordController
 */
function ForgotPasswordController($state, $timeout, PasswordResetInitService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.requestReset = requestReset;
    vm.email = '';
    vm.isSaving = false;
    vm.success = false;
    vm.errorMsg = null;

    $timeout(function () {
        angular.element('#email').focus();
    });

    function requestReset() {
        vm.isSaving = true;
        vm.success = false;
        PasswordResetInitService.create(vm.email,
            function (response) {
                vm.isSaving = false;
                vm.success = true;
            },
            function (response) {
                vm.isSaving = false;
                vm.success = false;
                vm.errorMsg = response.error_description;
            });
    }
}
/**
 * ResetPasswordController
 */
function ResetPasswordController($state, $stateParams, $timeout, PasswordResetFinishService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.resetPassword = resetPassword;
    vm.password = '';
    vm.confirmPassword = '';
    vm.success = false;
    vm.error = false;
    vm.isSaving = false;
    vm.keyMissing = angular.isUndefined($stateParams.key);

    $timeout(function () {
        angular.element('#password').focus();
    });

    function resetPassword() {
        vm.success = false;
        vm.error = false;
        vm.isSaving = true;
        PasswordResetFinishService.create({key: $stateParams.key, newPassword: vm.password},
            function (response) {
                vm.success = true;
                vm.isSaving = false;
            },
            function (response) {
                vm.success = false;
                vm.error = true;
                vm.isSaving = false;
            });
    }
}
/**
 * PasswordController
 */
function PasswordController($state, PasswordService, PrincipalService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.save = save;
    vm.isSaving = false;
    vm.passwordNotMatch = false;

    PrincipalService.identity().then(function (account) {
        vm.account = account;
    });

    function save() {
        if (vm.password !== vm.confirmPassword) {
            vm.passwordNotMatch = true;
        } else {
            vm.passwordNotMatch = false;
            vm.isSaving = true;
            PasswordService.update({'newPassword': vm.password},
                function (response) {
                    vm.isSaving = false;
                    $state.go('login');
                },
                function (response) {
                    vm.isSaving = false;
                });
        }
    }
}

/**
 * MetricsController
 *
 */
function MetricsController($state, $scope, $uibModal, MetricsService, metrics) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.cachesStats = {};
    vm.metrics = metrics;
    vm.refresh = refresh;
    vm.refreshThreadDumpData = refreshThreadDumpData;
    vm.servicesStats = {};
    vm.updatingMetrics = false;
    /**
     * Options for Doughnut chart
     */
    vm.doughnutOptions = {
        segmentShowStroke: true,
        segmentStrokeColor: '#fff',
        segmentStrokeWidth: 2,
        percentageInnerCutout: 45, // This is 0 for Pie charts
        animationSteps: 100,
        animationEasing: 'easeOutBounce',
        animateRotate: true,
        animateScale: false
    };

    vm.totalMemory = [
        {
            value: vm.metrics.gauges['jvm.memory.total.used'].value / 1000000,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: '已使用'
        },
        {
            value: (vm.metrics.gauges['jvm.memory.total.max'].value - vm.metrics.gauges['jvm.memory.total.used'].value) / 1000000,
            color: '#dedede',
            highlight: '#1ab394',
            label: '未使用'
        }
    ];

    vm.heapMemory = [
        {
            value: vm.metrics.gauges['jvm.memory.heap.used'].value / 1000000,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: '已使用'
        },
        {
            value: (vm.metrics.gauges['jvm.memory.heap.max'].value - vm.metrics.gauges['jvm.memory.heap.used'].value) / 1000000,
            color: '#dedede',
            highlight: '#1ab394',
            label: '未使用'
        }
    ];

    vm.edenSpaceMemory = [
        {
            value: vm.metrics.gauges['jvm.memory.pools.PS-Eden-Space.used'].value / 1000000,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: '已使用'
        },
        {
            value: (vm.metrics.gauges['jvm.memory.pools.PS-Eden-Space.max'].value - vm.metrics.gauges['jvm.memory.pools.PS-Eden-Space.used'].value) / 1000000,
            color: '#dedede',
            highlight: '#1ab394',
            label: '未使用'
        }
    ];

    vm.survivorSpaceMemory = [
        {
            value: vm.metrics.gauges['jvm.memory.pools.PS-Survivor-Space.used'].value / 1000000,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: '已使用'
        },
        {
            value: (vm.metrics.gauges['jvm.memory.pools.PS-Survivor-Space.max'].value - vm.metrics.gauges['jvm.memory.pools.PS-Survivor-Space.used'].value) / 1000000,
            color: '#dedede',
            highlight: '#1ab394',
            label: '未使用'
        }
    ];

    vm.oldSpaceMemory = [
        {
            value: vm.metrics.gauges['jvm.memory.pools.PS-Old-Gen.used'].value / 1000000,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: '已使用'
        },
        {
            value: (vm.metrics.gauges['jvm.memory.pools.PS-Old-Gen.max'].value - vm.metrics.gauges['jvm.memory.pools.PS-Old-Gen.used'].value) / 1000000,
            color: '#dedede',
            highlight: '#1ab394',
            label: '未使用'
        }
    ];

    vm.nonHeapMemory = [
        {
            value: vm.metrics.gauges['jvm.memory.non-heap.used'].value / 1000000,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: '已使用'
        },
        {
            value: (vm.metrics.gauges['jvm.memory.non-heap.committed'].value - vm.metrics.gauges['jvm.memory.non-heap.used'].value) / 1000000,
            color: '#dedede',
            highlight: '#1ab394',
            label: '未使用'
        }
    ];

    vm.runnableThreads = [
        {
            value: vm.metrics.gauges['jvm.threads.runnable.count'].value,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: 'Runnable'
        },
        {
            value: (vm.metrics.gauges['jvm.threads.count'].value - vm.metrics.gauges['jvm.threads.runnable.count'].value),
            color: '#dedede',
            highlight: '#1ab394',
            label: 'Others'
        }
    ];

    vm.timedWaitingThreads = [
        {
            value: vm.metrics.gauges['jvm.threads.timed_waiting.count'].value,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: 'Timed waiting'
        },
        {
            value: (vm.metrics.gauges['jvm.threads.count'].value - vm.metrics.gauges['jvm.threads.timed_waiting.count'].value),
            color: '#dedede',
            highlight: '#1ab394',
            label: 'Others'
        }
    ];

    vm.waitingThreads = [
        {
            value: vm.metrics.gauges['jvm.threads.waiting.count'].value,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: 'Waiting'
        },
        {
            value: (vm.metrics.gauges['jvm.threads.count'].value - vm.metrics.gauges['jvm.threads.waiting.count'].value),
            color: '#dedede',
            highlight: '#1ab394',
            label: 'Others'
        }
    ];

    vm.blockedThreads = [
        {
            value: vm.metrics.gauges['jvm.threads.blocked.count'].value,
            color: '#a3e1d4',
            highlight: '#1ab394',
            label: 'Blocked'
        },
        {
            value: (vm.metrics.gauges['jvm.threads.count'].value - vm.metrics.gauges['jvm.threads.blocked.count'].value),
            color: '#dedede',
            highlight: '#1ab394',
            label: 'Others'
        }
    ];

    $scope.$watch('vm.metrics', function (newValue) {
        vm.servicesStats = {};
        vm.cachesStats = {};
        angular.forEach(newValue.timers, function (value, key) {
            if (key.indexOf('controller.') !== -1) {
                var controllerStartIndex = key.indexOf('controller.');
                var offset = 'controller.'.length;
                vm.servicesStats[key.substr(controllerStartIndex + offset, key.length)] = value;
            }
            if (key.indexOf('service.impl.') !== -1) {
                var controllerStartIndex = key.indexOf('service.impl.');
                var offset = 'service.impl.'.length;
                vm.servicesStats[key.substr(controllerStartIndex + offset, key.length)] = value;
            }
            if (key.indexOf('net.sf.ehcache.Cache') !== -1) {
                // remove gets or puts
                var index = key.lastIndexOf('.');
                var newKey = key.substr(0, index);

                // Keep the name of the domain
                index = newKey.lastIndexOf('.');
                vm.cachesStats[newKey] = {
                    'name': newKey.substr(index + 1),
                    'value': value
                };
            }
        });
    });

    function refresh() {
        vm.updatingMetrics = true;
        MetricsService.getMetrics().then(function (promise) {
            vm.metrics = promise;
            vm.updatingMetrics = false;
        }, function (promise) {
            vm.metrics = promise.data;
            vm.updatingMetrics = false;
        });
    }

    function refreshThreadDumpData() {
        MetricsService.threadDump().then(function (data) {
            $uibModal.open({
                templateUrl: 'app/views/developer/metrics/metrics.modal.html',
                controller: 'MetricsDialogController',
                controllerAs: 'vm',
                size: 'lg',
                resolve: {
                    threadDump: function () {
                        return data.threads;
                    }
                }
            });
        });
    }
}

/**
 * MetricsDialogController
 *
 */
function MetricsDialogController($uibModalInstance, threadDump) {
    var vm = this;

    vm.cancel = cancel;
    vm.getLabelClass = getLabelClass;
    vm.threadDump = threadDump;
    vm.threadDumpAll = 0;
    vm.threadDumpBlocked = 0;
    vm.threadDumpRunnable = 0;
    vm.threadDumpTimedWaiting = 0;
    vm.threadDumpWaiting = 0;

    angular.forEach(threadDump, function (value) {
        if (value.threadState === 'RUNNABLE') {
            vm.threadDumpRunnable += 1;
        } else if (value.threadState === 'WAITING') {
            vm.threadDumpWaiting += 1;
        } else if (value.threadState === 'TIMED_WAITING') {
            vm.threadDumpTimedWaiting += 1;
        } else if (value.threadState === 'BLOCKED') {
            vm.threadDumpBlocked += 1;
        }
    });

    vm.threadDumpAll = vm.threadDumpRunnable + vm.threadDumpWaiting +
        vm.threadDumpTimedWaiting + vm.threadDumpBlocked;

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }

    function getLabelClass(threadState) {
        if (threadState === 'RUNNABLE') {
            return 'label-success';
        } else if (threadState === 'WAITING') {
            return 'label-info';
        } else if (threadState === 'TIMED_WAITING') {
            return 'label-warning';
        } else if (threadState === 'BLOCKED') {
            return 'label-danger';
        }
    }
}

function HealthController($state, HealthService, $uibModal) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.updatingHealth = true;
    vm.getLabelClass = getLabelClass;
    vm.refresh = refresh;
    vm.showHealth = showHealth;
    vm.baseName = HealthService.getBaseName;
    vm.subSystemName = HealthService.getSubSystemName;

    vm.refresh();

    function getLabelClass(statusState) {
        if (statusState === 'UP') {
            return 'label-primary';
        } else {
            return 'label-danger';
        }
    }

    function refresh() {
        vm.updatingHealth = true;
        HealthService.checkHealth().then(function (response) {
            vm.healthData = HealthService.transformHealthData(response);
            vm.updatingHealth = false;
        }, function (response) {
            vm.healthData = HealthService.transformHealthData(response.data);
            vm.updatingHealth = false;
        });
    }

    function showHealth(health) {
        $uibModal.open({
            templateUrl: 'app/views/developer/health/health.dialog.html',
            controller: 'HealthDialogController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: {
                currentHealth: function () {
                    return health;
                },
                baseName: function () {
                    return vm.baseName;
                },
                subSystemName: function () {
                    return vm.subSystemName;
                }
            }
        });
    }
}

function HealthDialogController($uibModalInstance, currentHealth, baseName, subSystemName) {
    var vm = this;

    vm.cancel = cancel;
    vm.currentHealth = currentHealth;
    vm.baseName = baseName;
    vm.subSystemName = subSystemName;

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

function ConfigurationController($state, ConfigurationService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.allConfiguration = null;
    vm.configuration = null;
    vm.configKeys = [];

    ConfigurationService.get().then(function (configuration) {
        vm.configuration = configuration;

        for (var config in configuration) {
            if (config.properties !== undefined) {
                vm.configKeys.push(Object.keys(config.properties));
            }
        }
    });
    ConfigurationService.getEnv().then(function (configuration) {
        vm.allConfiguration = configuration;
    });
}

function BeansController($state, $http, APP_NAME) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.items = null;
    vm.refresh = refresh;
    vm.refresh();

    function refresh() {
        $http.get('management/beans').then(function (response) {
            vm.items = [];
            angular.forEach(response.data['contexts'][APP_NAME]['beans'], function (val, key) {
                vm.items.push({bean: key, type: val.type, scope: val.scope, dependencies: val.dependencies});
            });
        });
    }
}

function MappingsController($state, $http, APP_NAME) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.items = [];
    vm.refresh = refresh;
    vm.refresh();

    function refresh() {
        $http.get('management/mappings').then(function (response) {
            var mappings = response.data['contexts'][APP_NAME]['mappings'];

            for (var key in mappings) {
                if (key === 'dispatcherServlets') {
                    angular.forEach(mappings[key]['dispatcherServlet'], function (v, k) {
                        vm.items.push({url: v.predicate, handler: v.handler});
                    });
                } else if (key === 'servletFilters') {
                    angular.forEach(mappings[key], function (v, k) {
                        vm.items.push({url: v.urlPatternMappings, handler: v.className});
                    });
                } else if (key === 'servlets') {
                    angular.forEach(mappings[key], function (v, k) {
                        vm.items.push({url: v.mappings, handler: v.className});
                    });
                }
            }
        });
    }
}

function HttpTraceController($state, $http) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.refresh = refresh;
    vm.refresh();

    function refresh() {
        $http.get('management/httptrace').then(function (response) {
            vm.items = response.data.traces;
        });
    }
}

/**
 * HttpSessionController
 */
function HttpSessionController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, HttpSessionService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        HttpSessionService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            principal: vm.criteria.principal
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'principal') {
            // default sort column
            result.push('principal,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            principal: vm.criteria.principal
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                HttpSessionService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

function AuditsController($state, $filter, AuditsService, ParseLinksUtils, PAGINATION_CONSTANTS) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.entities = null;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.page = 1;
    vm.itemsPerPage = 20;
    vm.previousMonth = previousMonth;
    // Previous 1 month
    vm.fromDate = null;
    // Tomorrow
    vm.toDate = null;
    vm.today = today;
    vm.totalItems = null;
    vm.predicate = 'auditEventDate';
    vm.reverse = false;

    vm.today();
    vm.previousMonth();
    vm.loadAll();

    function loadAll() {
        var dateFormat = 'yyyy-MM-dd';
        var fromDate = $filter('date')(vm.fromDate, dateFormat);
        var toDate = $filter('date')(vm.toDate, dateFormat);
        AuditsService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            sort: [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')],
            from: fromDate,
            to: toDate
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.entities = result;
        });
    }

    // Date picker configuration
    function today() {
        // Today + 1 day - needed if the current day must be included
        var today = new Date();
        vm.toDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1);
    }

    function previousMonth() {
        var fromDate = new Date();
        if (fromDate.getMonth() === 0) {
            fromDate = new Date(fromDate.getFullYear() - 1, 11, fromDate.getDate());
        } else {
            fromDate = new Date(fromDate.getFullYear(), fromDate.getMonth() - 1, fromDate.getDate());
        }
        vm.fromDate = fromDate;
    }
}

/**
 * TrackerController
 */
function TrackerController($cookies, $http, TrackerService) {
    // This controller uses a Websocket connection to receive user activities in real-time.
    var vm = this;

    vm.activities = [];

    TrackerService.receive().then(null, null, function (activity) {
        showActivity(activity);
    });

    function showActivity(activity) {
        var existingActivity = false;
        for (var index = 0; index < vm.activities.length; index++) {
            if (vm.activities[index].sessionId === activity.sessionId) {
                existingActivity = true;
                if (activity.page === 'logout') {
                    vm.activities.splice(index, 1);
                } else {
                    vm.activities[index] = activity;
                }
            }
        }
        if (!existingActivity && (activity.page !== 'logout')) {
            vm.activities.push(activity);
        }
    }

}

/**
 * DictListController
 */
function DictListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, DictService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.setEnabled = setEnabled;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        DictService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            dictName: vm.criteria.dictName
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'dictCode') {
            // default sort column
            result.push('dictCode,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            dictName: vm.criteria.dictName
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function setEnabled(entity, enabled) {
        entity.enabled = enabled;
        DictService.update(entity,
            function () {
                vm.loadAll();
            },
            function () {
                entity.enabled = !enabled;
            });
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                DictService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * DictDialogController
 */
function DictDialogController($state, $stateParams, $uibModalInstance, DictService, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.mode = $state.current.data.mode;
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;

    function save() {
        vm.isSaving = true;
        if (vm.mode == 'edit') {
            DictService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            DictService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

/**
 * DictItemListController
 */
function DictItemListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, DictService, DictItemService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.dicts = DictService.query();
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.setEnabled = setEnabled;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        DictItemService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            dictCode: vm.criteria.dictCode,
            dictItemName: vm.criteria.dictItemName
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'dictCode') {
            // default sort column
            result.push('dictCode,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            dictCode: vm.criteria.dictCode,
            dictItemName: vm.criteria.dictItemName
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function setEnabled(entity, enabled) {
        entity.enabled = enabled;
        DictItemService.update(entity,
            function () {
                vm.loadAll();
            },
            function () {
                entity.enabled = !enabled;
            });
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                DictItemService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * DictItemDialogController
 */
function DictItemDialogController($state, $stateParams, $uibModalInstance, DictService, DictItemService, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.dicts = DictService.query({enabled: true});
    vm.mode = $state.current.data.mode;
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;

    function save() {
        vm.isSaving = true;
        if (vm.mode == 'edit') {
            DictItemService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            DictItemService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

/**
 * LoggerController
 */
function LoggerController($state, LoggerService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.changeLevel = changeLevel;
    vm.query = query;

    vm.query();

    function query() {
        LoggerService.query({}, function (data) {
            vm.loggers = [];
            angular.forEach(data.loggers, function (val, key) {
                vm.loggers.push({name: key, level: val.effectiveLevel});
            });
        });
    }

    function changeLevel(name, level) {
        // The first argument is the path variable, the second one is request body
        LoggerService.changeLevel({name: name}, {configuredLevel: level}, function () {
            vm.query();
        });
    }
}

/**
 * ScheduleController
 */
function ScheduleController($state, $http) {
    var vm = this;
    vm.data = {};

    vm.pageTitle = $state.current.data.pageTitle;

    $http.get('management/scheduledtasks').then(function (response) {
        vm.data = response.data;
    });
}

/**
 * ControlController
 */
function ControlController($state, $http, AlertUtils) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.items = null;
    vm.shutdown = shutdown;

    function shutdown() {
        $http.post('management/shutdown').then(function (response) {
                AlertUtils.success('Shutdown successfully', {});
            },
            function (response) {
                AlertUtils.error('Shutdown failed', {});
            });
    }
}
/**
 * AuthorityListController
 */
function AuthorityListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, AuthorityService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.setEnabled = setEnabled;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        AuthorityService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort()
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'name') {
            // default sort column
            result.push('name,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function setEnabled(entity, enabled) {
        entity.enabled = enabled;
        AuthorityService.update(entity,
            function () {
                vm.loadAll();
            },
            function () {
                entity.enabled = !enabled;
            });
    }

    function del(name) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                AuthorityService.del({name: name},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * AuthorityDialogController
 */
function AuthorityDialogController($state, $stateParams, $uibModalInstance, AuthorityService, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.mode = $state.current.data.mode;
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;

    function save() {
        vm.isSaving = true;
        if (vm.mode == 'edit') {
            AuthorityService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            AuthorityService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

/**
 * UserListController
 */
function UserListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, UserService, PrincipalService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.currentAccount = null;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.setActive = setActive;
    vm.setEnabled = setEnabled;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;
    vm.resetPassword = resetPassword;

    vm.loadAll();

    PrincipalService.identity().then(function (account) {
        vm.currentAccount = account;
    });

    function loadAll() {
        UserService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            login: vm.criteria.login
        }, function (result, headers) {
            //hide anonymous user from user management: it's a required user for Spring Security
            for (var i in result) {
                if (result[i]['userName'] === 'anonymoususer') {
                    result.splice(i, co1);
                }
            }
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'modifiedTime') {
            // default sort column
            result.push('modifiedTime,desc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            login: vm.criteria.login
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function setActive(user, isActivated) {
        user.activated = isActivated;
        UserService.update(user, function () {
                vm.loadAll();
            },
            function () {
                user.activated = !isActivated;
            });
    }

    function setEnabled(entity, enabled) {
        entity.enabled = enabled;
        UserService.update(entity,
            function () {
                vm.loadAll();
            },
            function () {
                entity.enabled = !enabled;
            });
    }

    function del(userName) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                UserService.del({userName: userName},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }

    function resetPassword(userName) {
        AlertUtils.createResetPasswordConfirmation('密码恢复到初始值?', function (isConfirm) {
            if (isConfirm) {
                UserService.resetPassword({userName: userName},
                    function () {
                    });
            }
        });
    }
}

/**
 * UserDialogController
 */
function UserDialogController($state, $stateParams, $uibModalInstance, UserService, AccountService, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.mode = $state.current.data.mode;
    vm.authorities = AccountService.queryAuthorityNames({enabled: true});
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;

    if (vm.mode == 'create') {
        vm.entity.authorities = ["ROLE_USER"];
    }

    function save() {
        vm.isSaving = true;
        if (vm.mode == 'edit') {
            UserService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            UserService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

/**
 * UserDetailsController
 */
function UserDetailsController($state, $stateParams, entity, AuthServerService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.grandfatherPageTitle = $state.$current.parent.parent.data.pageTitle;
    vm.entity = entity;

    var authToken = AuthServerService.getToken();
    if (authToken) {
        vm.entity.profilePhotoUrl = '/api/user/profile-photo/' + vm.entity.userName + '?access_token=' + authToken.access_token;
    }
}

/**
 * AppListController
 */
function AppListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, AppService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.setEnabled = setEnabled;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        AppService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort()
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'name') {
            // default sort column
            result.push('name,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function setEnabled(entity, enabled) {
        entity.enabled = enabled;
        AppService.update(entity,
            function () {
                vm.loadAll();
            },
            function () {
                entity.enabled = !enabled;
            });
    }

    function del(name) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                AppService.del({name: name},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * AppDialogController
 */
function AppDialogController($state, $stateParams, $uibModalInstance, AppService, AccountService, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.mode = $state.current.data.mode;
    vm.authorities = AccountService.queryAuthorityNames({enabled: true});
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;

    function save() {
        vm.isSaving = true;
        if (vm.mode == 'edit') {
            AppService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            AppService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

/**
 * AppDetailsController
 */
function AppDetailsController($state, $stateParams, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.grandfatherPageTitle = $state.$current.parent.parent.data.pageTitle;
    vm.entity = entity;
}

/**
 * AdminMenuListController
 */
function AdminMenuListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, AdminMenuService, AppService, APP_NAME) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.apps = AppService.query();
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;
    vm.moveUp = moveUp;
    vm.moveDown = moveDown;

    vm.loadAll();

    function loadAll() {
        AdminMenuService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            appName: vm.criteria.app
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'modifiedTime') {
            // default sort column
            result.push('modifiedTime,desc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            app: vm.criteria.app
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                AdminMenuService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }

    function moveUp(id) {
        AdminMenuService.moveUp({id: id},
            function () {
                vm.loadAll()
            });
    }

    function moveDown(id) {
        AdminMenuService.moveDown({id: id},
            function () {
                vm.loadAll()
            });
    }
}

/**
 * AdminMenuDialogController
 */
function AdminMenuDialogController($state, $stateParams, $uibModalInstance, AdminMenuService, AppService, APP_NAME, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.mode = $state.current.data.mode;
    vm.apps = AppService.query();
    vm.searchParentMenus = searchParentMenus;
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;

    vm.searchParentMenus();

    function save() {
        vm.isSaving = true;
        vm.entity.level = 1;
        if (vm.entity.parentId) {
            vm.entity.level = 2;
        }
        if (vm.mode == 'edit') {
            AdminMenuService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            AdminMenuService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function searchParentMenus() {
        if (vm.entity && vm.entity.appName) {
            vm.parentMenus = AdminMenuService.queryParents({appName: vm.entity.appName, level: 1});
        }
        else {
            vm.parentMenus = [];
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

/**
 * AuthorityAdminMenuController
 */
function AuthorityAdminMenuController($state, AuthorityAdminMenuService, AppAuthorityService, AppService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.apps = AppService.query();
    vm.authorities = [];
    vm.allMenus = [];
    vm.isSaving = false;
    vm.searchAuthorities = searchAuthorities;
    vm.searchMenus = searchMenus;
    vm.save = save;

    function searchAuthorities() {
        if (vm.criteria && vm.criteria.appName) {
            vm.authorities = AppAuthorityService.query({appName: vm.criteria.appName});
        }
        else {
            vm.authorities = [];
        }
    }

    function searchMenus() {
        vm.allMenus = [];
        if (vm.criteria.authorityName) {
            AuthorityAdminMenuService.query({
                appName: vm.criteria.appName,
                authorityName: vm.criteria.authorityName
            }, function (response) {
                vm.allMenus = response;
            });
        }
    }

    function save() {
        vm.isSaving = true;
        if (vm.criteria.appName && vm.criteria.authorityName) {
            var adminMenuIds = getAllCheckIds(vm.allMenus, []);
            AuthorityAdminMenuService.update({
                    appName: vm.criteria.appName,
                    authorityName: vm.criteria.authorityName,
                    adminMenuIds: adminMenuIds
                },
                function (response) {
                    vm.isSaving = false;
                }, function (errorResponse) {
                    vm.isSaving = false;
                });
        }
    }

    function getAllCheckIds(allMenus, adminMenuIds) {
        for (var i = 0; i < allMenus.length; i++) {
            if (allMenus[i].checked) {
                adminMenuIds.push(allMenus[i].id);
            }
            if (allMenus[i].children) {
                getAllCheckIds(allMenus[i].children, adminMenuIds);
            }
        }
        return adminMenuIds;
    }
}

/**
 * OAuth2ClientListController
 */
function OAuth2ClientListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, OAuth2ClientService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        OAuth2ClientService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            clientId: vm.criteria.clientId
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'client_id') {
            // default sort column
            result.push('client_id,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            clientId: vm.criteria.clientId
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('数据有可能被其他数据所引用，删除之后可能出现一些问题，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                OAuth2ClientService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * OAuth2ClientDialogController
 */
function OAuth2ClientDialogController($scope, $state, $stateParams, $uibModalInstance, OAuth2ClientService, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.mode = $state.current.data.mode;
    vm.entity = entity;
    vm.isSaving = false;
    vm.save = save;
    vm.cancel = cancel;
    vm.addUri = addUri;
    vm.delUri = delUri;

    if (vm.mode == 'create') {
        vm.entity.redirect_uri.push("");
    }

    $scope.$watch('vm.entity.scope', function (newValue) {
        if (newValue) {
            if (_.isArray(newValue)) {
                vm.entity.scopeArray = newValue;
            } else {
                vm.entity.scopeArray = newValue.split(',');
            }
        }
        else {
            vm.entity.scopeArray = [];
        }
    });
    vm.entity.scopeArray = vm.entity.scope;


    function save() {
        vm.isSaving = true;

        vm.entity.autoapprove = _.intersection(vm.entity.autoapprove, vm.entity.scopeArray);

        if (vm.mode == 'edit') {
            OAuth2ClientService.update(vm.entity, onSaveSuccess, onSaveError);
        } else {
            OAuth2ClientService.create(vm.entity, onSaveSuccess, onSaveError);
        }
    }

    function onSaveSuccess(result) {
        vm.isSaving = false;
        $uibModalInstance.close(result);
    }

    function onSaveError(result) {
        vm.isSaving = false;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }

    function addUri() {
        vm.entity.redirect_uri.length++;
    }

    function delUri(index) {
        // Remove element
        vm.entity.redirect_uri.splice(index, 1);
    }
}

/**
 * OAuth2ClientDetailsController
 */
function OAuth2ClientDetailsController($state, $stateParams, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.grandfatherPageTitle = $state.$current.parent.parent.data.pageTitle;
    vm.entity = entity;
}

/**
 * OAuth2AccessTokenListController
 */
function OAuth2AccessTokenListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, OAuth2AccessTokenService, AuthServerService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.currentAccessToken = AuthServerService.getAccessToken();
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;
    vm.goRefreshToken = goRefreshToken;

    vm.loadAll();

    function loadAll() {
        OAuth2AccessTokenService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            tokenId: vm.criteria.tokenId,
            clientId: vm.criteria.clientId,
            userName: vm.criteria.userName,
            refreshToken: vm.criteria.refreshToken
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'modifiedTime') {
            // default sort column
            result.push('modifiedTime,desc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            tokenId: vm.criteria.tokenId,
            clientId: vm.criteria.clientId,
            userName: vm.criteria.userName,
            refreshToken: vm.criteria.refreshToken
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('删除会使该用户退出系统，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                OAuth2AccessTokenService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }

    function goRefreshToken(refreshToken) {
        $state.go('security.oauth2-refresh-token-list', {'tokenId': refreshToken});
    }
}

/**
 * OAuth2AccessTokenDetailsController
 */
function OAuth2AccessTokenDetailsController($state, $stateParams, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.grandfatherPageTitle = $state.$current.parent.parent.data.pageTitle;
    vm.entity = entity;
}

/**
 * OAuth2RefreshTokenListController
 */
function OAuth2RefreshTokenListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, OAuth2RefreshTokenService, AuthServerService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.currentRefreshToken = AuthServerService.getRefreshToken();
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        OAuth2RefreshTokenService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            tokenId: vm.criteria.tokenId,
            clientId: vm.criteria.clientId,
            userName: vm.criteria.userName
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'modifiedTime') {
            // default sort column
            result.push('modifiedTime,desc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            tokenId: vm.criteria.tokenId,
            clientId: vm.criteria.clientId,
            userName: vm.criteria.userName
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('删除会使该用户退出系统，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                OAuth2RefreshTokenService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * OAuth2RefreshTokenDetailsController
 */
function OAuth2RefreshTokenDetailsController($state, $stateParams, entity) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.grandfatherPageTitle = $state.$current.parent.parent.data.pageTitle;
    vm.entity = entity;
}

/**
 * OAuth2ApprovalListController
 */
function OAuth2ApprovalListController($state, AlertUtils, ParseLinksUtils, PAGINATION_CONSTANTS, pagingParams, criteria, OAuth2ApprovalService) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.links = null;
    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.checkPressEnter = checkPressEnter;
    vm.page = 1;
    vm.totalItems = null;
    vm.entities = [];
    vm.predicate = pagingParams.predicate;
    vm.reverse = pagingParams.ascending;
    vm.itemsPerPage = PAGINATION_CONSTANTS.itemsPerPage;
    vm.transition = transition;
    vm.criteria = criteria;
    vm.del = del;

    vm.loadAll();

    function loadAll() {
        OAuth2ApprovalService.query({
            page: pagingParams.page - 1,
            size: vm.itemsPerPage,
            sort: sort(),
            approvalId: vm.criteria.approvalId,
            clientId: vm.criteria.clientId,
            userName: vm.criteria.userName
        }, function (result, headers) {
            vm.links = ParseLinksUtils.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.page = pagingParams.page;
            vm.entities = result;
        });
    }

    function sort() {
        var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
        if (vm.predicate !== 'clientId') {
            // default sort column
            result.push('clientId,asc');
        }
        return result;
    }

    function loadPage(page) {
        vm.page = page;
        vm.transition();
    }

    function transition() {
        $state.transitionTo($state.$current, {
            page: vm.page,
            sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
            approvalId: vm.criteria.approvalId,
            clientId: vm.criteria.clientId,
            userName: vm.criteria.userName
        });
    }

    function checkPressEnter($event) {
        //按下enter键重新查询数据
        if ($event.keyCode == 13) {
            vm.transition();
        }
    }

    function del(id) {
        AlertUtils.createDeleteConfirmation('删除会使该用户退出系统，您确定删除吗?', function (isConfirm) {
            if (isConfirm) {
                OAuth2ApprovalService.del({id: id},
                    function () {
                        vm.loadAll();
                    },
                    function () {
                    });
            }
        });
    }
}

/**
 * OAuth2ApprovalDetailsController
 */
function OAuth2ApprovalDetailsController($state, $stateParams, entity, $scope, $rootScope) {
    var vm = this;

    vm.pageTitle = $state.current.data.pageTitle;
    vm.parentPageTitle = $state.$current.parent.data.pageTitle;
    vm.grandfatherPageTitle = $state.$current.parent.parent.data.pageTitle;
    vm.entity = entity;
}
