var is429App = angular.module('is429App', ['ngRoute','firebase']);

is429App.config(['$routeProvider', function($routeProvider) {
			$routeProvider
				.when('/',
					{
						templateUrl: 'partials/home.html',
						activetab: 'home'
					})
				.when('/home',
					{
						templateUrl: 'partials/home.html',
						activetab: 'home'
					})
				.when('/howtoplay',
					{
						templateUrl: 'partials/howtoplay.html',
						activetab: 'howtoplay'
					})
				.when('/leaderboard',
					{
						templateUrl: 'partials/leaderboard.html',
						activetab: 'leaderboard'
					})
				.otherwise({redirectTo: '/'});
		}]);
		
is429App.controller("appController",["$scope", "$route", "$firebase", "$firebaseSimpleLogin",
	function($scope, $route, $firebase, $firebaseSimpleLogin) {
		$scope.$route = $route;
		var ref = new Firebase("https://is429-project.firebaseio.com/");
		$scope.auth = $firebaseSimpleLogin(ref);
	}
]);