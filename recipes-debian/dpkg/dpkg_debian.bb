#
# base recipe: meta/recipes-devtools/dpkg/dpkg_1.17.4.bb
# base branch: daisy
#

require dpkg.inc

PR = "${INC_PR}.2"

inherit systemd

DEPENDS = "zlib bzip2 perl ncurses xz"
DEPENDS_class-native = "bzip2-replacement-native \
                        zlib-native \
                        gettext-native \
                        perl-native \
                        make-native \
                        xz-native \
                        "
RDEPENDS_${PN} = "${VIRTUAL-RUNTIME_update-alternatives} xz"
RDEPENDS_${PN}_class-native = "xz-native"

PARALLEL_MAKE = ""

inherit autotools gettext perlnative pkgconfig

python () {
    if not bb.utils.contains('DISTRO_FEATURES', 'sysvinit', True, False, d):
        pn = d.getVar('PN', True)
        d.setVar('SYSTEMD_SERVICE_%s' % (pn), 'dpkg-configure.service')
}

export PERL = "${bindir}/perl"
PERL_class-native = "${STAGING_BINDIR_NATIVE}/perl-native/perl"

export PERL_LIBDIR = "${libdir}/perl"
PERL_LIBDIR_class-native = "${libdir}/perl-native/perl"

# Follow debian/rules
EXTRA_OECONF = " \
	--sbindir=${base_sbindir} \
	--localstatedir=${localstatedir} \
	--with-zlib \
	--with-liblzma \
	--with-bz2 \
"
PACKAGECONFIG ?= "${@base_contains('DISTRO_FEATURES', 'selinux', 'selinux', '', d)}"
PACKAGECONFIG[selinux] = "--with-selinux,--without-selinux,libselinux"

do_configure () {
	echo >> ${S}/m4/compiler.m4
	sed -i -e 's#PERL_LIBDIR=.*$#PERL_LIBDIR="${libdir}/perl"#' ${S}/configure
	autotools_do_configure
}

do_install_append () {
	rm ${D}${bindir}/update-alternatives
	if [ "${PN}" = "dpkg-native" ]; then
		sed -i -e 's|^#!.*${bindir}/perl-native.*/perl|#!/usr/bin/env nativeperl|' ${D}${bindir}/dpkg-*
	else
		sed -i -e 's|^#!.*${bindir}/perl-native.*/perl|#!/usr/bin/env perl|' ${D}${bindir}/dpkg-*
	fi

	if ${@base_contains('DISTRO_FEATURES','sysvinit','false','true',d)};then
		install -d ${D}${systemd_unitdir}/system
		install -m 0644 ${WORKDIR}/dpkg-configure.service ${D}${systemd_unitdir}/system/
		sed -i -e 's,@BASE_BINDIR@,${base_bindir},g' \
			-e 's,@SYSCONFDIR@,${sysconfdir},g' \
			-e 's,@BINDIR@,${bindir},g' \
			-e 's,@SYSTEMD_UNITDIR@,${systemd_unitdir},g' \
			${D}${systemd_unitdir}/system/dpkg-configure.service
	fi

	# Install configuration files and links follow Debian
	install -d ${D}${sysconfdir}/cron.daily
	install -d ${D}${sysconfdir}/logrotate.d
	install -m 0644 ${S}/debian/dpkg.cfg ${D}${sysconfdir}/${DPN}/
	install -m 0755 ${S}/debian/dpkg.cron.daily ${D}${sysconfdir}/cron.daily/dpkg
	install -m 0644 ${S}/debian/dpkg.logrotate ${D}${sysconfdir}/logrotate.d/dpkg

	# Follow debian/dpkg.links
	install -d ${D}${sbindir}
	ln -s ../bin/dpkg-divert ${D}${sbindir}/dpkg-divert
	ln -s ../bin/dpkg-statoverride ${D}${sbindir}/dpkg-statoverride

	# Follow debian/dselect.install
	install -m 0644 ${S}/debian/dselect.cfg ${D}${sysconfdir}/dpkg

	# Follow debian/dpkg-dev.install
	cp ${S}/debian/shlibs.* ${D}${sysconfdir}/dpkg
}

PACKAGES =+ "dselect ${PN}-perl libdpkg-dev"

FILES_dselect = " \
	${sysconfdir}/dpkg/dselect* \
	${bindir}/dselect \
	${libdir}/dpkg/methods \
	${libdir}/perl/Dselect/* \
	${localestatedir}/lib/dpkg/methods \
"
FILES_${PN}-perl = "${libdir}/perl ${libdir}/${DPN}/parsechangelog"
FILES_libdpkg-dev = "${includedir} ${libdir}/pkgconfig"
FILES_${PN}-dev += " \
	${sysconfdir}/dpkg/shlibs.* \
	${datadir}/dpkg/*.mk \
	${bindir}/dpkg-architecture \
	${bindir}/dpkg-buildflags \
	${bindir}/dpkg-buildpackage \
	${bindir}/dpkg-checkbuilddeps \
	${bindir}/dpkg-distaddfile \
	${bindir}/dpkg-genchanges \
	${bindir}/dpkg-gencontrol \
	${bindir}/dpkg-gensymbols \
	${bindir}/dpkg-mergechangelogs \
	${bindir}/dpkg-name \
	${bindir}/dpkg-parsechangelog \
	${bindir}/dpkg-scanpackages \
	${bindir}/dpkg-scansources \
	${bindir}/dpkg-shlibdeps \
	${bindir}/dpkg-source \
	${bindir}/dpkg-vendor \
"

PKG_${PN}-perl = "libdpkg-perl"

RDEPENDS_dselect += "${PN}"
RDEPENDS_${PN}-dev += "${PN}-perl"
RDEPENDS_${PN}-perl += "${PN}"

BBCLASSEXTEND = "native"
