package com.caixabank.absis3

import org.junit.Test


class RouteUtilitiesTest extends GroovyTestCase {


    @Test
    public void testIfAliveRouteExistsThenReturnThatRouteIsAlive() {
        String mappedRoutes = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com, sca-micro-server-1.tst.int.srv.caixabank.com, sca-micro-server-1.tst.ext.srv.caixabank.com, sca-micro-server-1.tst1.int.srv.caixabank.com'
        String aliveRoute = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com';
        boolean isAlive = RouteUtilities.isRouteAlive(aliveRoute, mappedRoutes);
        assertTrue(isAlive);
    }

    @Test
    public void testIfAliveRouteExistsThenReturnThatRouteIsAliveWithUpperCase() {
        String mappedRoutes = 'SCA-micro-server-1-beta.tst.int.srv.caixabank.com, SCA-micro-server-1.tst.int.srv.caixabank.com, SCA-micro-server-1.tst.ext.srv.caixabank.com, SCA-micro-server-1.tst1.int.srv.caixabank.com'
        String aliveRoute = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com';
        boolean isAlive = RouteUtilities.isRouteAlive(aliveRoute, mappedRoutes);
        assertTrue(isAlive);
    }

    @Test
    public void testIfThereIsNotMappedRoutesThenReturnThatRouteIsNotAlive() {
        String mappedRoutes = ''
        String aliveRoute = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com';
        boolean isAlive = RouteUtilities.isRouteAlive(aliveRoute, mappedRoutes);
        assertFalse(isAlive);
    }

    @Test
    public void testIfAliveRouteNotExistsThenReturnThatRouteIsAlive() {
        String mappedRoutes = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com, sca-micro-server-1.tst.int.srv.caixabank.com, sca-micro-server-1.tst.ext.srv.caixabank.com, sca-micro-server-1.tst1.int.srv.caixabank.com'
        String aliveRoute = 'notvalid-micro-server-1-beta.tst.int.srv.caixabank.com';
        boolean isAlive = RouteUtilities.isRouteAlive(aliveRoute, mappedRoutes);
        assertFalse(isAlive);
    }

    @Test
    public void testOnlyBetaUrlsWhenOtherRoutesExist() {
        String mappedRoutes = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com, sca-micro-server-1.tst.int.srv.caixabank.com, sca-micro-server-1.tst.ext.srv.caixabank.com, sca-micro-server-1.tst1.int.srv.caixabank.com'
        boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(mappedRoutes)
        assertFalse(onlyBeta);
    }

    @Test
    public void testOnlyBetaUrlWhenAllRoutesAreBeta() {
        String mappedRoutes = 'sca-micro-server-1-beta.tst.int.srv.caixabank.com, sca-micro-server-1-beta.tst.ext.srv.caixabank.com, sca-micro-server-1-beta.tst1.int.srv.caixabank.com'
        boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(mappedRoutes)
        assertTrue(onlyBeta);
    }

    @Test
    public void testOnlyBetaUrlWhenAllRoutesAreBetaWithUpperCase() {
        String mappedRoutes = 'SCA-micro-server-1-beta.tst.int.srv.caixabank.com, SCA-micro-server-1-beta.tst.ext.srv.caixabank.com, SCA-micro-server-1-beta.tst1.int.srv.caixabank.com'
        boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(mappedRoutes)
        assertTrue(onlyBeta);
    }


    @Test
    public void testOnlyBetaUrlWhenLastRouteIsBeta() {
        String mappedRoutes = 'sca-micro-server-1.tst.int.srv.caixabank.com, sca-micro-server-1.tst.ext.srv.caixabank.com, sca-micro-server-1-beta.tst1.int.srv.caixabank.com'
        boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(mappedRoutes)
        assertFalse(onlyBeta);
    }

    @Test
    public void testGetAppInfo() {
        String appAndRoute = 'service-manager-micro-server-1            started             1/1          1G        1G      service-manager-micro-server-1-beta.pro.ext.srv.caixabank.com, service-manager-micro-server-1-beta.pro.int.srv.caixabank.com, service-manager-micro-server-1-beta.pro1.int.srv.caixabank.com'
        AppCloud appRoute = RouteUtilities.getAppInfo(appAndRoute)
        assertToString(appRoute.app, 'service-manager-micro-server-1')
        assertToString(appRoute.routes, 'service-manager-micro-server-1-beta.pro.ext.srv.caixabank.com,service-manager-micro-server-1-beta.pro.int.srv.caixabank.com,service-manager-micro-server-1-beta.pro1.int.srv.caixabank.com')
    }

    @Test
    public void testGetAppInfoAndValidateIsNotOnlyBeta() {
        String appAndRoute = 'service-manager-micro-server-1            started             1/1          1G        1G      service-manager-micro-server-1-beta.pro.ext.srv.caixabank.com, service-manager-micro-server-1.pro.int.srv.caixabank.com, service-manager-micro-server-1-beta.pro1.int.srv.caixabank.com'
        AppCloud appRoute = RouteUtilities.getAppInfo(appAndRoute)
        boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(appRoute.routes)
        assertFalse(onlyBeta)
    }


    @Test
    public void testGetAppInfoAndValidateOnlyBeta() {
        String appAndRoute = 'service-manager-micro-server-1            started             1/1          1G        1G      service-manager-micro-server-1-beta.pro.ext.srv.caixabank.com, service-manager-micro-server-1-beta.pro.int.srv.caixabank.com, service-manager-micro-server-1-beta.pro1.int.srv.caixabank.com'
        AppCloud appRoute = RouteUtilities.getAppInfo(appAndRoute)
        boolean onlyBeta = RouteUtilities.isOnlyBetaUrls(appRoute.routes)
        assertTrue(onlyBeta)
    }
}
