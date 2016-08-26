DESCRIPTION = "Linux Containers userspace tools"

LICENSE = "GPL-2+ & BSD-2-Clause & LGPL-2.1+"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c \
                    file://src/include/getline.c;endline=28;md5=0027dba404eeb169fe513cf9cf875c6f \
                    file://src/include/lxcmntent.c;endline=19;md5=7d2ae8e27e363ef071df24ceb18a4ddf \
                    "

PR = "r0"

DEPENDS = "libxml2 libcap"

inherit debian-package

# starting udhcpc service by default on start is useless so disable it from busybox template
SRC_URI += "file://lxc-disable-udhcp-from-busybox-template.patch"

inherit autotools pkgconfig

EXTRA_OECONF = "--disable-rpath \
                --enable-capabilities \
                --disable-cgmanager --disable-mutex-debugging \
                --enable-bash --with-distro=debian \
                --with-init-script=sysvinit,systemd \
                "

do_install() {
        oe_runmake 'DESTDIR="${D}"' 'SYSTEMD_UNIT_DIR="${systemd_system_unitdir}"' install

        rm -rf ${D}${includedir}
        rm -rf ${D}${libdir}/pkgconfig
        
        # The /var/cache/lxc directory created by the Makefile
        # is wiped out in volatile, we need to create this at boot.
        rm -rf ${D}${localstatedir}/cache
        install -d ${D}${sysconfdir}/default/volatiles
        echo "d root root 0755 ${localstatedir}/cache/lxc none" \
             > ${D}${sysconfdir}/default/volatiles/99_lxc

        for i in `grep -l "#! */bin/bash" ${D}${datadir}/lxc/hooks/*`;
        do
            sed -e 's|#! */bin/bash|#!/bin/sh|' -i $i
        done
}

FILES_${PN}-dbg += "${libexecdir}/lxc/.debug"
FILES_${PN} += "${base_libdir} \
                ${systemd_unitdir} \
                ${systemd_system_unitdir} \
                ${systemd_system_unitdir}/lxc.service \
                "

RDEPENDS_${PN} = "bash \
                  rsync \
                  gzip \
                  libcap-bin \
                  perl-module-strict \
                  perl-module-getopt-long \
                  perl-module-vars \
                  perl-module-warnings-register \
                  perl-module-exporter \
                  perl-module-constant \
                  perl-module-overload \
                  perl-module-exporter-heavy \
                  "

pkg_postinst_${PN}() {
        if [ -z "$D" ] && [ -e ${sysconfdir}/init.d/populate-volatile.sh ] ; then
                ${sysconfdir}/init.d/populate-volatile.sh update
        fi
}

