#
# No base recipe
#

SUMMARY = "PostgreSQL is a powerful, open source relational database system."
DESCRIPTION = "\
    PostgreSQL is an advanced Object-Relational database management system \
    (DBMS) that supports almost all SQL constructs (including \
    transactions, subselects and user-defined types and functions). The \
    postgresql package includes the client programs and libraries that \
    you'll need to access a PostgreSQL DBMS server.  These PostgreSQL \
    client programs are programs that directly manipulate the internal \
    structure of PostgreSQL databases on a PostgreSQL server. These client \
    programs can be located on the same machine with the PostgreSQL \
    server, or may be on a remote machine which accesses a PostgreSQL \
    server over a network connection. This package contains the docs \
    in HTML for the whole package, as well as command-line utilities for \
    managing PostgreSQL databases on a PostgreSQL server. \
    \
    If you want to manipulate a PostgreSQL database on a local or remote \
    PostgreSQL server, you need this package. You also need to install \
    this package if you're installing the postgresql-server package. \
"
HOMEPAGE = "http://www.postgresql.com"

PR = "r0"

inherit debian-package

DPN = "postgresql-9.4"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://COPYRIGHT;md5=7d847a9b446ddfe187acfac664189672"

DEPENDS = "zlib readline krb5 tcl libxml2 libxslt libselinux python"

inherit autotools-brokensep pkgconfig perlnative pythonnative systemd

export BUILD_SYS
export HOST_SYS
export STAGING_INCDIR
export STAGING_LIBDIR

MAJOR_VER = "9.4"
TCL_VER = "8.6"

COMMON_CONFIGURE_FLAGS = " \
	--mandir=${datadir}/${PN}/${MAJOR_VER}/man \
	--docdir=${datadir}/doc/${PN}-doc-${MAJOR_VER} \
	--sysconfdir=${sysconfdir}/${PN}-common \
	--datarootdir=${datadir} \
	--datadir=${datadir}/${PN}/${MAJOR_VER} \
	--bindir=${libdir}/${PN}/${MAJOR_VER}/${base_bindir} \
	--libdir=${libdir} \
	--libexecdir=${libdir}/${PN}/ \
	--includedir=${includedir}/${PN} \
	--enable-nls \
	--enable-integer-datetimes \
	--enable-thread-safety \
	--enable-debug \
	--disable-rpath \
	--with-uuid=e2fs \
	--with-gnu-ld \
	--with-pgport=5432 \
	--with-system-tzdata=${STAGING_DATADIR}/zoneinfo \
"

# Disable --with-perl --with-python
#
EXTRA_OECONF = "--with-tcl --with-pam --with-openssl --with-selinux \
	      --with-libxml --with-libxslt --with-tclconfig=${STAGING_LIBDIR}/tcl${TCL_VER} \
	      --with-includes=${STAGING_INCDIR}/tcl${TCL_VER} ${COMMON_CONFIGURE_FLAGS} \
	      "
# 
# Fix error the install log indicates that host include and/or library paths were used
# 
do_compile_prepend() {
	sed -i -e "s:-L/usr/lib:-L${STAGING_LIBDIR}:g" ${S}/src/Makefile.global		
	sed -i -e "s:-L/usr/include:-L${STAGING_INCDIR}:g" ${S}/src/Makefile.global		
}

do_install_append() {
	# Follow Deian, some files belong to /usr/bin
	install -d ${D}${bindir}
	cp ${D}${libdir}/${PN}/${MAJOR_VER}${base_bindir}/pg_config ${D}${bindir}
	mv ${D}${libdir}/${PN}/${MAJOR_VER}${base_bindir}/ecpg ${D}${bindir}
}

PACKAGES =+ "libecpg-compat libecpg-dev libecpg libpgtypes libpq-dev libpq ${PN}-client-${MAJOR_VER} \
	     ${PN}-pltcl-${MAJOR_VER} ${PN}-plperl-${MAJOR_VER} ${PN}-contrib-${MAJOR_VER} \
	     ${PN}-plpython-${MAJOR_VER} ${PN}-plpython3-${MAJOR_VER} ${PN}-server-dev-${MAJOR_VER}"

FILES_libecpg-compat += " \
		${libdir}/libecpg_compat.so.*"
FILES_libecpg-dev += " \
		${bindir}/ecpg ${libdir}/libecpg*.so \
                ${libdir}/libpgtypes.so \
                ${libdir}/pkgconfig/libpgtypes.pc \
                ${libdir}/pkgconfig/libecpg*.pc \
                ${includedir}/${PN}/sqlda*.h \
                ${includedir}/${PN}/sql3types.h \
                ${includedir}/${PN}/pgtypes*.h \
                ${includedir}/${PN}/ecpg*.h \
                ${includedir}/${PN}/informix \
                "
FILES_libecpg += "${libdir}/libecpg.so.*"
FILES_libpgtypes += "libpgtypes.so.*.*"
FILES_libpq-dev += " \
		${bindir}/pg_config \
                ${libdir}/pkgconfig/libpq.pc \
                ${includedir}/${PN}/pg_config*.h \
                ${includedir}/${PN}/postgres_ext.h \
                ${includedir}/${PN}/libpq/* \
                ${includedir}/${PN}/libpq-* \
                ${includedir}/${PN}/internal/* \
                ${libdir}/libpq.so"
FILES_libpq += "${libdir}/libpq.so.*"
FILES_${PN}-client-${MAJOR_VER} += " \
                ${libdir}/${PN}/${MAJOR_VER}/bin/clusterdb \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_dumpall \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_dump \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_basebackup \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_isready \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_recvlogical \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_receivexlog \
                ${libdir}/${PN}/${MAJOR_VER}/bin/createdb \
                ${libdir}/${PN}/${MAJOR_VER}/bin/createlang \
                ${libdir}/${PN}/${MAJOR_VER}/bin/createuser \
                ${libdir}/${PN}/${MAJOR_VER}/bin/dropdb \
                ${libdir}/${PN}/${MAJOR_VER}/bin/droplang \
                ${libdir}/${PN}/${MAJOR_VER}/bin/dropuser \
                ${libdir}/${PN}/${MAJOR_VER}/bin/reindexdb \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_restore \
                ${libdir}/${PN}/${MAJOR_VER}/bin/psql \
                ${libdir}/${PN}/${MAJOR_VER}/bin/vacuumdb \
		"
FILES_${PN}-contrib-${MAJOR_VER} += " \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_archivecleanup \
                ${libdir}/${PN}/${MAJOR_VER}/bin/oid2name \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pgbench \
                ${libdir}/${PN}/${MAJOR_VER}/bin/vacuumlo \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_standby \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_test_fsync \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_test_timing \
                ${libdir}/${PN}/${MAJOR_VER}/lib/*.so \
                "
FILES_${PN}-plperl-${MAJOR_VER} += " \
                ${libdir}/${PN}/${MAJOR_VER}/lib/plperl.so \
                "
#
#FILES_${PN}-plpython-${MAJOR_VER} += " \
#               ${libdir}/${PN}/${MAJOR_VER}/lib/plpython2.so \
#               "

FILES_${PN}-pltcl-${MAJOR_VER} += " \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pltcl_* \
                ${libdir}/${PN}/${MAJOR_VER}/lib/pltcl.so \
                "
FILES_${PN}-server-dev-${MAJOR_VER} += " \
                ${libdir}/${PN}/${MAJOR_VER}/bin/pg_config \
                ${libdir}/${PN}/${MAJOR_VER}/lib/pgxs/src/test/regress/* \
                ${libdir}/${PN}/${MAJOR_VER}/lib/pgxs/src/config \
                ${libdir}/${PN}/${MAJOR_VER}/lib/pgxs/src/Makefiles.* \
                ${libdir}/${PN}/${MAJOR_VER}/lib/pgxs/src/makefiles \
                ${libdir}/${PN}/${MAJOR_VER}/lib/pgxs/src/nls-global.mk \
                ${includedir}/* \
                "

FILES_${PN}-dbg += "${libdir}/${PN}/${MAJOR_VER}/bin/.debug/*"
FILES_${PN}-dbg += "${libdir}/${PN}/${MAJOR_VER}/lib/.debug/*"
FILES_${PN}-dbg += "${libdir}/${PN}/${MAJOR_VER}/lib/pgxs/src/test/regress/.debug/*"

