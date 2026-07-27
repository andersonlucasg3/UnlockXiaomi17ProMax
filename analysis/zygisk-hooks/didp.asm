
/data/adb/modules/deviceidchanger/zygisk/arm64-v8a.so:	file format elf64-littleaarch64

Disassembly of section .text:

0000000000005144 <.text>:
    5144: d503245f     	bti	c
    5148: d503201f     	nop
    514c: 10029020     	adr	x0, 0xa350
    5150: 14000428     	b	0x61f0 <__cxa_finalize@plt>
    5154: d503245f     	bti	c
    5158: d65f03c0     	ret
    515c: d503245f     	bti	c
    5160: 17fffffd     	b	0x5154 <.text+0x10>
    5164: d503245f     	bti	c
    5168: b4000060     	cbz	x0, 0x5174 <.text+0x30>
    516c: aa0003f0     	mov	x16, x0
    5170: d61f0200     	br	x16
    5174: d65f03c0     	ret
    5178: d503245f     	bti	c
    517c: aa0003e1     	mov	x1, x0
    5180: d503201f     	nop
    5184: 10ffff00     	adr	x0, 0x5164 <.text+0x20>
    5188: d503201f     	nop
    518c: 10028e22     	adr	x2, 0xa350
    5190: 1400041c     	b	0x6200 <__cxa_atexit@plt>
    5194: d503245f     	bti	c
    5198: d503201f     	nop
    519c: 10028da3     	adr	x3, 0xa350
    51a0: 1400041c     	b	0x6210 <__register_atfork@plt>

00000000000051a4 <zygisk_module_entry>:
    51a4: 14000001     	b	0x51a8 <zygisk_module_entry+0x4>
    51a8: a9be7bfd     	stp	x29, x30, [sp, #-0x20]!
    51ac: f9000bf3     	str	x19, [sp, #0x10]
    51b0: 910003fd     	mov	x29, sp
    51b4: b0000048     	adrp	x8, 0xe000
    51b8: aa0103f3     	mov	x19, x1
    51bc: f9034d00     	str	x0, [x8, #0x698]
    51c0: d503201f     	nop
    51c4: 1004a7a8     	adr	x8, 0xe6b8
    51c8: 08dffd08     	ldarb	w8, [x8]
    51cc: 360002e8     	tbz	w8, #0x0, 0x5228 <zygisk_module_entry+0x84>
    51d0: d503201f     	nop
    51d4: 1004a8e8     	adr	x8, 0xe6f0
    51d8: 08dffd08     	ldarb	w8, [x8]
    51dc: 36000488     	tbz	w8, #0x0, 0x526c <zygisk_module_entry+0xc8>
    51e0: f9400408     	ldr	x8, [x0, #0x8]
    51e4: d503201f     	nop
    51e8: 1004a6c1     	adr	x1, 0xe6c0
    51ec: d63f0100     	blr	x8
    51f0: 36000160     	tbz	w0, #0x0, 0x521c <zygisk_module_entry+0x78>
    51f4: d503201f     	nop
    51f8: 1004a540     	adr	x0, 0xe6a0
    51fc: d503201f     	nop
    5200: 1004a4c1     	adr	x1, 0xe698
    5204: f9400008     	ldr	x8, [x0]
    5208: f9400103     	ldr	x3, [x8]
    520c: aa1303e2     	mov	x2, x19
    5210: f9400bf3     	ldr	x19, [sp, #0x10]
    5214: a8c27bfd     	ldp	x29, x30, [sp], #0x20
    5218: d61f0060     	br	x3
    521c: f9400bf3     	ldr	x19, [sp, #0x10]
    5220: a8c27bfd     	ldp	x29, x30, [sp], #0x20
    5224: d65f03c0     	ret
    5228: d503201f     	nop
    522c: 1004a468     	adr	x8, 0xe6b8
    5230: f9000fa0     	str	x0, [x29, #0x18]
    5234: aa0803e0     	mov	x0, x8
    5238: 940003fa     	bl	0x6220 <__cxa_guard_acquire@plt>
    523c: 2a0003e8     	mov	w8, w0
    5240: f9400fa0     	ldr	x0, [x29, #0x18]
    5244: 34fffc68     	cbz	w8, 0x51d0 <zygisk_module_entry+0x2c>
    5248: b0000048     	adrp	x8, 0xe000
    524c: b0000029     	adrp	x9, 0xa000
    5250: 910da129     	add	x9, x9, #0x368
    5254: d503201f     	nop
    5258: 1004a300     	adr	x0, 0xe6b8
    525c: f9035109     	str	x9, [x8, #0x6a0]
    5260: 940003f4     	bl	0x6230 <__cxa_guard_release@plt>
    5264: f9400fa0     	ldr	x0, [x29, #0x18]
    5268: 17ffffda     	b	0x51d0 <zygisk_module_entry+0x2c>
    526c: d503201f     	nop
    5270: 1004a408     	adr	x8, 0xe6f0
    5274: f9000fa0     	str	x0, [x29, #0x18]
    5278: aa0803e0     	mov	x0, x8
    527c: 940003e9     	bl	0x6220 <__cxa_guard_acquire@plt>
    5280: 2a0003e8     	mov	w8, w0
    5284: f9400fa0     	ldr	x0, [x29, #0x18]
    5288: 34fffac8     	cbz	w8, 0x51e0 <zygisk_module_entry+0x3c>
    528c: d503201f     	nop
    5290: 1004a188     	adr	x8, 0xe6c0
    5294: 52800089     	mov	w9, #0x4                // =4
    5298: d503201f     	nop
    529c: 1004a02a     	adr	x10, 0xe6a0
    52a0: d503201f     	nop
    52a4: 1004a260     	adr	x0, 0xe6f0
    52a8: a9002909     	stp	x9, x10, [x8]
    52ac: d503201f     	nop
    52b0: 100017a9     	adr	x9, 0x55a4 <zygisk_module_entry+0x400>
    52b4: d503201f     	nop
    52b8: 100017ca     	adr	x10, 0x55b0 <zygisk_module_entry+0x40c>
    52bc: a9012909     	stp	x9, x10, [x8, #0x10]
    52c0: d503201f     	nop
    52c4: 100017c9     	adr	x9, 0x55bc <zygisk_module_entry+0x418>
    52c8: d503201f     	nop
    52cc: 100017ea     	adr	x10, 0x55c8 <zygisk_module_entry+0x424>
    52d0: a9022909     	stp	x9, x10, [x8, #0x20]
    52d4: 940003d7     	bl	0x6230 <__cxa_guard_release@plt>
    52d8: f9400fa0     	ldr	x0, [x29, #0x18]
    52dc: 17ffffc1     	b	0x51e0 <zygisk_module_entry+0x3c>
    52e0: a9008801     	stp	x1, x2, [x0, #0x8]
    52e4: d65f03c0     	ret
    52e8: a9bd7bfd     	stp	x29, x30, [sp, #-0x30]!
    52ec: f9000bf5     	str	x21, [sp, #0x10]
    52f0: a9024ff4     	stp	x20, x19, [sp, #0x20]
    52f4: 910003fd     	mov	x29, sp
    52f8: f9401c28     	ldr	x8, [x1, #0x38]
    52fc: aa0103f4     	mov	x20, x1
    5300: aa0003f3     	mov	x19, x0
    5304: f9400101     	ldr	x1, [x8]
    5308: b40003e1     	cbz	x1, 0x5384 <zygisk_module_entry+0x1e0>
    530c: f9400a60     	ldr	x0, [x19, #0x10]
    5310: aa1f03e2     	mov	x2, xzr
    5314: f9400008     	ldr	x8, [x0]
    5318: f942a508     	ldr	x8, [x8, #0x548]
    531c: d63f0100     	blr	x8
    5320: b4000320     	cbz	x0, 0x5384 <zygisk_module_entry+0x1e0>
    5324: 39400008     	ldrb	w8, [x0]
    5328: aa0003f5     	mov	x21, x0
    532c: 340001e8     	cbz	w8, 0x5368 <zygisk_module_entry+0x1c4>
    5330: aa1303e0     	mov	x0, x19
    5334: aa1503e1     	mov	x1, x21
    5338: 9400002b     	bl	0x53e4 <zygisk_module_entry+0x240>
    533c: f9400a68     	ldr	x8, [x19, #0x10]
    5340: f9401e89     	ldr	x9, [x20, #0x38]
    5344: 2a0003f4     	mov	w20, w0
    5348: aa1503e2     	mov	x2, x21
    534c: f940010a     	ldr	x10, [x8]
    5350: f9400121     	ldr	x1, [x9]
    5354: aa0803e0     	mov	x0, x8
    5358: f942a949     	ldr	x9, [x10, #0x550]
    535c: d63f0120     	blr	x9
    5360: 36000134     	tbz	w20, #0x0, 0x5384 <zygisk_module_entry+0x1e0>
    5364: 14000012     	b	0x53ac <zygisk_module_entry+0x208>
    5368: f9400a60     	ldr	x0, [x19, #0x10]
    536c: f9401e88     	ldr	x8, [x20, #0x38]
    5370: aa1503e2     	mov	x2, x21
    5374: f9400009     	ldr	x9, [x0]
    5378: f9400101     	ldr	x1, [x8]
    537c: f942a928     	ldr	x8, [x9, #0x550]
    5380: d63f0100     	blr	x8
    5384: f9400668     	ldr	x8, [x19, #0x8]
    5388: f9400108     	ldr	x8, [x8]
    538c: f9401d02     	ldr	x2, [x8, #0x38]
    5390: b40000e2     	cbz	x2, 0x53ac <zygisk_module_entry+0x208>
    5394: f9400100     	ldr	x0, [x8]
    5398: 52800021     	mov	w1, #0x1                // =1
    539c: a9424ff4     	ldp	x20, x19, [sp, #0x20]
    53a0: f9400bf5     	ldr	x21, [sp, #0x10]
    53a4: a8c37bfd     	ldp	x29, x30, [sp], #0x30
    53a8: d61f0040     	br	x2
    53ac: a9424ff4     	ldp	x20, x19, [sp, #0x20]
    53b0: f9400bf5     	ldr	x21, [sp, #0x10]
    53b4: a8c37bfd     	ldp	x29, x30, [sp], #0x30
    53b8: d65f03c0     	ret
    53bc: 140000c3     	b	0x56c8 <zygisk_module_entry+0x524>
    53c0: f9400408     	ldr	x8, [x0, #0x8]
    53c4: f9400108     	ldr	x8, [x8]
    53c8: f9401d02     	ldr	x2, [x8, #0x38]
    53cc: b4000082     	cbz	x2, 0x53dc <zygisk_module_entry+0x238>
    53d0: f9400100     	ldr	x0, [x8]
    53d4: 52800021     	mov	w1, #0x1                // =1
    53d8: d61f0040     	br	x2
    53dc: d65f03c0     	ret
    53e0: d65f03c0     	ret
    53e4: a9bc7bfd     	stp	x29, x30, [sp, #-0x40]!
    53e8: f9000bf7     	str	x23, [sp, #0x10]
    53ec: a90257f6     	stp	x22, x21, [sp, #0x20]
    53f0: a9034ff4     	stp	x20, x19, [sp, #0x30]
    53f4: 910003fd     	mov	x29, sp
    53f8: f9400408     	ldr	x8, [x0, #0x8]
    53fc: aa0103f3     	mov	x19, x1
    5400: f9400109     	ldr	x9, [x8]
    5404: f9402128     	ldr	x8, [x9, #0x40]
    5408: b40001a8     	cbz	x8, 0x543c <zygisk_module_entry+0x298>
    540c: f9400120     	ldr	x0, [x9]
    5410: d63f0100     	blr	x8
    5414: 37f80140     	tbnz	w0, #0x1f, 0x543c <zygisk_module_entry+0x298>
    5418: d503201f     	nop
    541c: 30fdbd61     	adr	x1, 0xbc9
    5420: 2a1f03e2     	mov	w2, wzr
    5424: 2a0003f4     	mov	w20, w0
    5428: 94000386     	bl	0x6240 <openat@plt>
    542c: 2a0003f5     	mov	w21, w0
    5430: 2a1403e0     	mov	w0, w20
    5434: 94000387     	bl	0x6250 <close@plt>
    5438: 36f800f5     	tbz	w21, #0x1f, 0x5454 <zygisk_module_entry+0x2b0>
    543c: f0ffffc0     	adrp	x0, 0x0
    5440: 912fc000     	add	x0, x0, #0xbf0
    5444: 2a1f03e1     	mov	w1, wzr
    5448: 94000386     	bl	0x6260 <open@plt>
    544c: 37f80a00     	tbnz	w0, #0x1f, 0x558c <zygisk_module_entry+0x3e8>
    5450: 2a0003f5     	mov	w21, w0
    5454: aa1f03f6     	mov	x22, xzr
    5458: d503201f     	nop
    545c: 100494f4     	adr	x20, 0xe6f8
    5460: 529ffff7     	mov	w23, #0xffff            // =65535
    5464: cb1602e2     	sub	x2, x23, x22
    5468: 8b160281     	add	x1, x20, x22
    546c: 2a1503e0     	mov	w0, w21
    5470: 94000380     	bl	0x6270 <read@plt>
    5474: f100001f     	cmp	x0, #0x0
    5478: 540000ed     	b.le	0x5494 <zygisk_module_entry+0x2f0>
    547c: 8b160016     	add	x22, x0, x22
    5480: eb1702df     	cmp	x22, x23
    5484: 54ffff0b     	b.lt	0x5464 <zygisk_module_entry+0x2c0>
    5488: 2a1503e0     	mov	w0, w21
    548c: 94000371     	bl	0x6250 <close@plt>
    5490: 14000005     	b	0x54a4 <zygisk_module_entry+0x300>
    5494: 2a1503e0     	mov	w0, w21
    5498: 9400036e     	bl	0x6250 <close@plt>
    549c: f10006df     	cmp	x22, #0x1
    54a0: 5400076b     	b.lt	0x558c <zygisk_module_entry+0x3e8>
    54a4: 2a1f03f7     	mov	w23, wzr
    54a8: 38366a9f     	strb	wzr, [x20, x22]
    54ac: 14000003     	b	0x54b8 <zygisk_module_entry+0x314>
    54b0: 910006b4     	add	x20, x21, #0x1
    54b4: b4000555     	cbz	x21, 0x555c <zygisk_module_entry+0x3b8>
    54b8: 39400288     	ldrb	w8, [x20]
    54bc: 34000508     	cbz	w8, 0x555c <zygisk_module_entry+0x3b8>
    54c0: aa1403e0     	mov	x0, x20
    54c4: 52800141     	mov	w1, #0xa                // =10
    54c8: 9400036e     	bl	0x6280 <strchr@plt>
    54cc: aa0003f5     	mov	x21, x0
    54d0: b4000040     	cbz	x0, 0x54d8 <zygisk_module_entry+0x334>
    54d4: 390002bf     	strb	wzr, [x21]
    54d8: aa1403e0     	mov	x0, x20
    54dc: 9400036d     	bl	0x6290 <strlen@plt>
    54e0: b40000c0     	cbz	x0, 0x54f8 <zygisk_module_entry+0x354>
    54e4: 8b000288     	add	x8, x20, x0
    54e8: 385ff109     	ldurb	w9, [x8, #-0x1]
    54ec: 7100353f     	cmp	w9, #0xd
    54f0: 54000041     	b.ne	0x54f8 <zygisk_module_entry+0x354>
    54f4: 381ff11f     	sturb	wzr, [x8, #-0x1]
    54f8: aa1403e0     	mov	x0, x20
    54fc: 52800f81     	mov	w1, #0x7c               // =124
    5500: 94000360     	bl	0x6280 <strchr@plt>
    5504: b4fffd60     	cbz	x0, 0x54b0 <zygisk_module_entry+0x30c>
    5508: aa0003f6     	mov	x22, x0
    550c: 3900001f     	strb	wzr, [x0]
    5510: aa1403e0     	mov	x0, x20
    5514: aa1303e1     	mov	x1, x19
    5518: 94000362     	bl	0x62a0 <strcmp@plt>
    551c: 35fffca0     	cbnz	w0, 0x54b0 <zygisk_module_entry+0x30c>
    5520: 910006d4     	add	x20, x22, #0x1
    5524: 528007a1     	mov	w1, #0x3d               // =61
    5528: aa1403e0     	mov	x0, x20
    552c: 94000355     	bl	0x6280 <strchr@plt>
    5530: b4fffc00     	cbz	x0, 0x54b0 <zygisk_module_entry+0x30c>
    5534: aa0003e1     	mov	x1, x0
    5538: eb14001f     	cmp	x0, x20
    553c: 54fffba0     	b.eq	0x54b0 <zygisk_module_entry+0x30c>
    5540: aa1403e0     	mov	x0, x20
    5544: 3800143f     	strb	wzr, [x1], #0x1
    5548: 9400002a     	bl	0x55f0 <zygisk_module_entry+0x44c>
    554c: 7100001f     	cmp	w0, #0x0
    5550: 1a9f17e8     	cset	w8, eq
    5554: 2a170117     	orr	w23, w8, w23
    5558: 17ffffd6     	b	0x54b0 <zygisk_module_entry+0x30c>
    555c: 36000197     	tbz	w23, #0x0, 0x558c <zygisk_module_entry+0x3e8>
    5560: 94000057     	bl	0x56bc <zygisk_module_entry+0x518>
    5564: 2a0003e4     	mov	w4, w0
    5568: f0ffffc1     	adrp	x1, 0x0
    556c: 91295021     	add	x1, x1, #0xa54
    5570: f0ffffc2     	adrp	x2, 0x0
    5574: 91298442     	add	x2, x2, #0xa61
    5578: 52800080     	mov	w0, #0x4                // =4
    557c: aa1303e3     	mov	x3, x19
    5580: 9400034c     	bl	0x62b0 <__android_log_print@plt>
    5584: 52800020     	mov	w0, #0x1                // =1
    5588: 14000002     	b	0x5590 <zygisk_module_entry+0x3ec>
    558c: 2a1f03e0     	mov	w0, wzr
    5590: a9434ff4     	ldp	x20, x19, [sp, #0x30]
    5594: f9400bf7     	ldr	x23, [sp, #0x10]
    5598: a94257f6     	ldp	x22, x21, [sp, #0x20]
    559c: a8c47bfd     	ldp	x29, x30, [sp], #0x40
    55a0: d65f03c0     	ret
    55a4: f9400008     	ldr	x8, [x0]
    55a8: f9400502     	ldr	x2, [x8, #0x8]
    55ac: d61f0040     	br	x2
    55b0: f9400008     	ldr	x8, [x0]
    55b4: f9400902     	ldr	x2, [x8, #0x10]
    55b8: d61f0040     	br	x2
    55bc: f9400008     	ldr	x8, [x0]
    55c0: f9400d02     	ldr	x2, [x8, #0x18]
    55c4: d61f0040     	br	x2
    55c8: f9400008     	ldr	x8, [x0]
    55cc: f9401102     	ldr	x2, [x8, #0x20]
    55d0: d61f0040     	br	x2
    55d4: b00000c8     	adrp	x8, 0x1e000
    55d8: b00000c0     	adrp	x0, 0x1e000
    55dc: 911bf000     	add	x0, x0, #0x6fc
    55e0: 2a1f03e1     	mov	w1, wzr
    55e4: 5282e002     	mov	w2, #0x1700             // =5888
    55e8: b906f91f     	str	wzr, [x8, #0x6f8]
    55ec: 14000335     	b	0x62c0 <memset@plt>
    55f0: aa0003e8     	mov	x8, x0
    55f4: 12800000     	mov	w0, #-0x1               // =-1
    55f8: b4000608     	cbz	x8, 0x56b8 <zygisk_module_entry+0x514>
    55fc: a9bc7bfd     	stp	x29, x30, [sp, #-0x40]!
    5600: f9000bf7     	str	x23, [sp, #0x10]
    5604: a90257f6     	stp	x22, x21, [sp, #0x20]
    5608: a9034ff4     	stp	x20, x19, [sp, #0x30]
    560c: 910003fd     	mov	x29, sp
    5610: aa0103f3     	mov	x19, x1
    5614: b40004a1     	cbz	x1, 0x56a8 <zygisk_module_entry+0x504>
    5618: 39400109     	ldrb	w9, [x8]
    561c: 34000449     	cbz	w9, 0x56a4 <zygisk_module_entry+0x500>
    5620: aa0803e0     	mov	x0, x8
    5624: aa0803f5     	mov	x21, x8
    5628: 9400031a     	bl	0x6290 <strlen@plt>
    562c: aa0003f4     	mov	x20, x0
    5630: aa1303e0     	mov	x0, x19
    5634: 94000317     	bl	0x6290 <strlen@plt>
    5638: aa0003e8     	mov	x8, x0
    563c: f1016e9f     	cmp	x20, #0x5b
    5640: 12800000     	mov	w0, #-0x1               // =-1
    5644: 54000328     	b.hi	0x56a8 <zygisk_module_entry+0x504>
    5648: f1016d1f     	cmp	x8, #0x5b
    564c: 540002e8     	b.hi	0x56a8 <zygisk_module_entry+0x504>
    5650: b00000d6     	adrp	x22, 0x1e000
    5654: b986fad7     	ldrsw	x23, [x22, #0x6f8]
    5658: 71007eff     	cmp	w23, #0x1f
    565c: 5400026c     	b.gt	0x56a8 <zygisk_module_entry+0x504>
    5660: 52801709     	mov	w9, #0xb8               // =184
    5664: aa1503e1     	mov	x1, x21
    5668: b00000ca     	adrp	x10, 0x1e000
    566c: 911bf14a     	add	x10, x10, #0x6fc
    5670: 9b292af5     	smaddl	x21, w23, w9, x10
    5674: 91000682     	add	x2, x20, #0x1
    5678: aa0803f4     	mov	x20, x8
    567c: aa1503e0     	mov	x0, x21
    5680: 94000314     	bl	0x62d0 <memcpy@plt>
    5684: 910172a0     	add	x0, x21, #0x5c
    5688: 91000682     	add	x2, x20, #0x1
    568c: aa1303e1     	mov	x1, x19
    5690: 94000310     	bl	0x62d0 <memcpy@plt>
    5694: 2a1f03e0     	mov	w0, wzr
    5698: 110006e8     	add	w8, w23, #0x1
    569c: b906fac8     	str	w8, [x22, #0x6f8]
    56a0: 14000002     	b	0x56a8 <zygisk_module_entry+0x504>
    56a4: 12800000     	mov	w0, #-0x1               // =-1
    56a8: a9434ff4     	ldp	x20, x19, [sp, #0x30]
    56ac: f9400bf7     	ldr	x23, [sp, #0x10]
    56b0: a94257f6     	ldp	x22, x21, [sp, #0x20]
    56b4: a8c47bfd     	ldp	x29, x30, [sp], #0x40
    56b8: d65f03c0     	ret
    56bc: b00000c8     	adrp	x8, 0x1e000
    56c0: b946f900     	ldr	w0, [x8, #0x6f8]
    56c4: d65f03c0     	ret
    56c8: b00000c8     	adrp	x8, 0x1e000
    56cc: b946f908     	ldr	w8, [x8, #0x6f8]
    56d0: 340008e8     	cbz	w8, 0x57ec <zygisk_module_entry+0x648>
    56d4: d101c3ff     	sub	sp, sp, #0x70
    56d8: a9047bfd     	stp	x29, x30, [sp, #0x40]
    56dc: a90557f6     	stp	x22, x21, [sp, #0x50]
    56e0: a9064ff4     	stp	x20, x19, [sp, #0x60]
    56e4: 910103fd     	add	x29, sp, #0x40
    56e8: 528004e0     	mov	w0, #0x27               // =39
    56ec: 940002fd     	bl	0x62e0 <sysconf@plt>
    56f0: f100041f     	cmp	x0, #0x1
    56f4: d503201f     	nop
    56f8: 100264c8     	adr	x8, 0xa390
    56fc: 52820009     	mov	w9, #0x1000             // =4096
    5700: d00000d3     	adrp	x19, 0x1f000
    5704: 91388273     	add	x19, x19, #0xe20
    5708: f81e83a8     	stur	x8, [x29, #-0x18]
    570c: 52800088     	mov	w8, #0x4                // =4
    5710: 9a80b129     	csel	x9, x9, x0, lt
    5714: 90000000     	adrp	x0, 0x5000
    5718: 9133b000     	add	x0, x0, #0xcec
    571c: d10063a1     	sub	x1, x29, #0x18
    5720: a9007e7f     	stp	xzr, xzr, [x19]
    5724: b900127f     	str	wzr, [x19, #0x10]
    5728: b81f03a8     	stur	w8, [x29, #-0x10]
    572c: f81f83a9     	stur	x9, [x29, #-0x8]
    5730: 940002f0     	bl	0x62f0 <dl_iterate_phdr@plt>
    5734: 29401263     	ldp	w3, w4, [x19]
    5738: d00000d5     	adrp	x21, 0x1f000
    573c: d00000d6     	adrp	x22, 0x1f000
    5740: d00000d4     	adrp	x20, 0x1f000
    5744: 29411a65     	ldp	w5, w6, [x19, #0x8]
    5748: d00000c9     	adrp	x9, 0x1f000
    574c: b9401267     	ldr	w7, [x19, #0x10]
    5750: f94702a8     	ldr	x8, [x21, #0xe00]
    5754: f94706ca     	ldr	x10, [x22, #0xe08]
    5758: f9470a8b     	ldr	x11, [x20, #0xe10]
    575c: f9470d29     	ldr	x9, [x9, #0xe18]
    5760: f0ffffc1     	adrp	x1, 0x0
    5764: 91295021     	add	x1, x1, #0xa54
    5768: f0ffffc2     	adrp	x2, 0x0
    576c: 912bd042     	add	x2, x2, #0xaf4
    5770: 52800080     	mov	w0, #0x4                // =4
    5774: a90127eb     	stp	x11, x9, [sp, #0x10]
    5778: a9002be8     	stp	x8, x10, [sp]
    577c: 940002cd     	bl	0x62b0 <__android_log_print@plt>
    5780: b9401263     	ldr	w3, [x19, #0x10]
    5784: 340000e3     	cbz	w3, 0x57a0 <zygisk_module_entry+0x5fc>
    5788: f0ffffc1     	adrp	x1, 0x0
    578c: 91295021     	add	x1, x1, #0xa54
    5790: f0ffffc2     	adrp	x2, 0x0
    5794: 912b0042     	add	x2, x2, #0xac0
    5798: 528000c0     	mov	w0, #0x6                // =6
    579c: 940002c5     	bl	0x62b0 <__android_log_print@plt>
    57a0: f94702a8     	ldr	x8, [x21, #0xe00]
    57a4: b5000168     	cbnz	x8, 0x57d0 <zygisk_module_entry+0x62c>
    57a8: f94706c8     	ldr	x8, [x22, #0xe08]
    57ac: b5000128     	cbnz	x8, 0x57d0 <zygisk_module_entry+0x62c>
    57b0: f9470a88     	ldr	x8, [x20, #0xe10]
    57b4: b50000e8     	cbnz	x8, 0x57d0 <zygisk_module_entry+0x62c>
    57b8: f0ffffc1     	adrp	x1, 0x0
    57bc: 91295021     	add	x1, x1, #0xa54
    57c0: f0ffffc2     	adrp	x2, 0x0
    57c4: 9127c042     	add	x2, x2, #0x9f0
    57c8: 528000c0     	mov	w0, #0x6                // =6
    57cc: 940002b9     	bl	0x62b0 <__android_log_print@plt>
    57d0: d00000c8     	adrp	x8, 0x1f000
    57d4: b94e2900     	ldr	w0, [x8, #0xe28]
    57d8: a9464ff4     	ldp	x20, x19, [sp, #0x60]
    57dc: a94557f6     	ldp	x22, x21, [sp, #0x50]
    57e0: a9447bfd     	ldp	x29, x30, [sp, #0x40]
    57e4: 9101c3ff     	add	sp, sp, #0x70
    57e8: d65f03c0     	ret
    57ec: 2a1f03e0     	mov	w0, wzr
    57f0: d65f03c0     	ret
    57f4: a9bc7bfd     	stp	x29, x30, [sp, #-0x40]!
    57f8: f9000bf7     	str	x23, [sp, #0x10]
    57fc: a90257f6     	stp	x22, x21, [sp, #0x20]
    5800: a9034ff4     	stp	x20, x19, [sp, #0x30]
    5804: 910003fd     	mov	x29, sp
    5808: aa0003f4     	mov	x20, x0
    580c: aa0103f3     	mov	x19, x1
    5810: b40001c0     	cbz	x0, 0x5848 <zygisk_module_entry+0x6a4>
    5814: b00000c8     	adrp	x8, 0x1e000
    5818: b946f916     	ldr	w22, [x8, #0x6f8]
    581c: 710006df     	cmp	w22, #0x1
    5820: 5400014b     	b.lt	0x5848 <zygisk_module_entry+0x6a4>
    5824: b00000d5     	adrp	x21, 0x1e000
    5828: 911bf2b5     	add	x21, x21, #0x6fc
    582c: aa1503e0     	mov	x0, x21
    5830: aa1403e1     	mov	x1, x20
    5834: 9400029b     	bl	0x62a0 <strcmp@plt>
    5838: 340008a0     	cbz	w0, 0x594c <zygisk_module_entry+0x7a8>
    583c: f10006d6     	subs	x22, x22, #0x1
    5840: 9102e2b5     	add	x21, x21, #0xb8
    5844: 54ffff41     	b.ne	0x582c <zygisk_module_entry+0x688>
    5848: aa1f03f5     	mov	x21, xzr
    584c: b0000020     	adrp	x0, 0xa000
    5850: f942e001     	ldr	x1, [x0, #0x5c0]
    5854: 91170000     	add	x0, x0, #0x5c0
    5858: d63f0020     	blr	x1
    585c: d53bd056     	mrs	x22, TPIDR_EL0
    5860: b8606ac8     	ldr	w8, [x22, x0]
    5864: 34000368     	cbz	w8, 0x58d0 <zygisk_module_entry+0x72c>
    5868: b40005f5     	cbz	x21, 0x5924 <zygisk_module_entry+0x780>
    586c: f0ffffc1     	adrp	x1, 0x0
    5870: 91295021     	add	x1, x1, #0xa54
    5874: f0ffffc2     	adrp	x2, 0x0
    5878: 912d8042     	add	x2, x2, #0xb60
    587c: 52800080     	mov	w0, #0x4                // =4
    5880: aa1403e3     	mov	x3, x20
    5884: aa1503e4     	mov	x4, x21
    5888: 9400028a     	bl	0x62b0 <__android_log_print@plt>
    588c: aa1503e0     	mov	x0, x21
    5890: 94000280     	bl	0x6290 <strlen@plt>
    5894: 52800b68     	mov	w8, #0x5b               // =91
    5898: f1016c1f     	cmp	x0, #0x5b
    589c: aa0003f4     	mov	x20, x0
    58a0: 9a883016     	csel	x22, x0, x8, lo
    58a4: aa1303e0     	mov	x0, x19
    58a8: aa1503e1     	mov	x1, x21
    58ac: aa1603e2     	mov	x2, x22
    58b0: 94000288     	bl	0x62d0 <memcpy@plt>
    58b4: 38366a7f     	strb	wzr, [x19, x22]
    58b8: 2a1403e0     	mov	w0, w20
    58bc: a9434ff4     	ldp	x20, x19, [sp, #0x30]
    58c0: f9400bf7     	ldr	x23, [sp, #0x10]
    58c4: a94257f6     	ldp	x22, x21, [sp, #0x20]
    58c8: a8c47bfd     	ldp	x29, x30, [sp], #0x40
    58cc: d65f03c0     	ret
    58d0: 52800028     	mov	w8, #0x1                // =1
    58d4: b8206ac8     	str	w8, [x22, x0]
    58d8: f100029f     	cmp	x20, #0x0
    58dc: f0ffffc8     	adrp	x8, 0x0
    58e0: 91308108     	add	x8, x8, #0xc20
    58e4: f0ffffc9     	adrp	x9, 0x0
    58e8: 91292929     	add	x9, x9, #0xa4a
    58ec: 9a940103     	csel	x3, x8, x20, eq
    58f0: f0ffffc8     	adrp	x8, 0x0
    58f4: 912ed108     	add	x8, x8, #0xbb4
    58f8: f10002bf     	cmp	x21, #0x0
    58fc: f0ffffc1     	adrp	x1, 0x0
    5900: 912ad821     	add	x1, x1, #0xab6
    5904: 9a880124     	csel	x4, x9, x8, eq
    5908: f0ffffc2     	adrp	x2, 0x0
    590c: 912ab042     	add	x2, x2, #0xaac
    5910: aa0003f7     	mov	x23, x0
    5914: 52800080     	mov	w0, #0x4                // =4
    5918: 94000266     	bl	0x62b0 <__android_log_print@plt>
    591c: b8376adf     	str	wzr, [x22, x23]
    5920: b5fffa75     	cbnz	x21, 0x586c <zygisk_module_entry+0x6c8>
    5924: d00000c8     	adrp	x8, 0x1f000
    5928: f9470102     	ldr	x2, [x8, #0xe00]
    592c: b4000142     	cbz	x2, 0x5954 <zygisk_module_entry+0x7b0>
    5930: aa1403e0     	mov	x0, x20
    5934: aa1303e1     	mov	x1, x19
    5938: a9434ff4     	ldp	x20, x19, [sp, #0x30]
    593c: f9400bf7     	ldr	x23, [sp, #0x10]
    5940: a94257f6     	ldp	x22, x21, [sp, #0x20]
    5944: a8c47bfd     	ldp	x29, x30, [sp], #0x40
    5948: d61f0040     	br	x2
    594c: 910172b5     	add	x21, x21, #0x5c
    5950: 17ffffbf     	b	0x584c <zygisk_module_entry+0x6a8>
    5954: 2a1f03f4     	mov	w20, wzr
    5958: b4fffb13     	cbz	x19, 0x58b8 <zygisk_module_entry+0x714>
    595c: 3900027f     	strb	wzr, [x19]
    5960: 17ffffd6     	b	0x58b8 <zygisk_module_entry+0x714>
    5964: a9bb7bfd     	stp	x29, x30, [sp, #-0x50]!
    5968: a90167fc     	stp	x28, x25, [sp, #0x10]
    596c: a9025ff8     	stp	x24, x23, [sp, #0x20]
    5970: a90357f6     	stp	x22, x21, [sp, #0x30]
    5974: a9044ff4     	stp	x20, x19, [sp, #0x40]
    5978: 910003fd     	mov	x29, sp
    597c: d10983ff     	sub	sp, sp, #0x260
    5980: d00000d6     	adrp	x22, 0x1f000
    5984: aa0103f3     	mov	x19, x1
    5988: f94706c8     	ldr	x8, [x22, #0xe08]
    598c: b40008e0     	cbz	x0, 0x5aa8 <zygisk_module_entry+0x904>
    5990: d00000c9     	adrp	x9, 0x1f000
    5994: f9470929     	ldr	x9, [x9, #0xe10]
    5998: b4000889     	cbz	x9, 0x5aa8 <zygisk_module_entry+0x904>
    599c: b4000868     	cbz	x8, 0x5aa8 <zygisk_module_entry+0x904>
    59a0: aa0203f4     	mov	x20, x2
    59a4: 910183e1     	add	x1, sp, #0x60
    59a8: 910013e2     	add	x2, sp, #0x4
    59ac: aa0003f7     	mov	x23, x0
    59b0: d63f0120     	blr	x9
    59b4: 37f80740     	tbnz	w0, #0x1f, 0x5a9c <zygisk_module_entry+0x8f8>
    59b8: b00000c8     	adrp	x8, 0x1e000
    59bc: b946f918     	ldr	w24, [x8, #0x6f8]
    59c0: 7100071f     	cmp	w24, #0x1
    59c4: 5400014b     	b.lt	0x59ec <zygisk_module_entry+0x848>
    59c8: b00000d5     	adrp	x21, 0x1e000
    59cc: 911bf2b5     	add	x21, x21, #0x6fc
    59d0: 910183e1     	add	x1, sp, #0x60
    59d4: aa1503e0     	mov	x0, x21
    59d8: 94000232     	bl	0x62a0 <strcmp@plt>
    59dc: 340000c0     	cbz	w0, 0x59f4 <zygisk_module_entry+0x850>
    59e0: f1000718     	subs	x24, x24, #0x1
    59e4: 9102e2b5     	add	x21, x21, #0xb8
    59e8: 54ffff41     	b.ne	0x59d0 <zygisk_module_entry+0x82c>
    59ec: aa1f03f5     	mov	x21, xzr
    59f0: 14000002     	b	0x59f8 <zygisk_module_entry+0x854>
    59f4: 910172b5     	add	x21, x21, #0x5c
    59f8: b0000020     	adrp	x0, 0xa000
    59fc: f942e001     	ldr	x1, [x0, #0x5c0]
    5a00: 91170000     	add	x0, x0, #0x5c0
    5a04: d63f0020     	blr	x1
    5a08: d53bd058     	mrs	x24, TPIDR_EL0
    5a0c: b8606b08     	ldr	w8, [x24, x0]
    5a10: 34000228     	cbz	w8, 0x5a54 <zygisk_module_entry+0x8b0>
    5a14: b4000455     	cbz	x21, 0x5a9c <zygisk_module_entry+0x8f8>
    5a18: f0ffffc1     	adrp	x1, 0x0
    5a1c: 91295021     	add	x1, x1, #0xa54
    5a20: f0ffffc2     	adrp	x2, 0x0
    5a24: 91289442     	add	x2, x2, #0xa25
    5a28: 910183e3     	add	x3, sp, #0x60
    5a2c: 52800080     	mov	w0, #0x4                // =4
    5a30: aa1503e4     	mov	x4, x21
    5a34: 9400021f     	bl	0x62b0 <__android_log_print@plt>
    5a38: b40003f3     	cbz	x19, 0x5ab4 <zygisk_module_entry+0x910>
    5a3c: 910183e1     	add	x1, sp, #0x60
    5a40: aa1403e0     	mov	x0, x20
    5a44: aa1503e2     	mov	x2, x21
    5a48: 2a1f03e3     	mov	w3, wzr
    5a4c: d63f0260     	blr	x19
    5a50: 14000019     	b	0x5ab4 <zygisk_module_entry+0x910>
    5a54: 52800028     	mov	w8, #0x1                // =1
    5a58: b8206b08     	str	w8, [x24, x0]
    5a5c: f10002bf     	cmp	x21, #0x0
    5a60: f0ffffc8     	adrp	x8, 0x0
    5a64: 912ed108     	add	x8, x8, #0xbb4
    5a68: f0ffffc9     	adrp	x9, 0x0
    5a6c: 91292929     	add	x9, x9, #0xa4a
    5a70: f0ffffc1     	adrp	x1, 0x0
    5a74: 912ad821     	add	x1, x1, #0xab6
    5a78: 9a880124     	csel	x4, x9, x8, eq
    5a7c: f0ffffc2     	adrp	x2, 0x0
    5a80: 912dec42     	add	x2, x2, #0xb7b
    5a84: 910183e3     	add	x3, sp, #0x60
    5a88: aa0003f9     	mov	x25, x0
    5a8c: 52800080     	mov	w0, #0x4                // =4
    5a90: 94000208     	bl	0x62b0 <__android_log_print@plt>
    5a94: b8396b1f     	str	wzr, [x24, x25]
    5a98: b5fffc15     	cbnz	x21, 0x5a18 <zygisk_module_entry+0x874>
    5a9c: f94706c8     	ldr	x8, [x22, #0xe08]
    5aa0: aa1403e2     	mov	x2, x20
    5aa4: aa1703e0     	mov	x0, x23
    5aa8: b4000068     	cbz	x8, 0x5ab4 <zygisk_module_entry+0x910>
    5aac: aa1303e1     	mov	x1, x19
    5ab0: d63f0100     	blr	x8
    5ab4: 910983ff     	add	sp, sp, #0x260
    5ab8: a9444ff4     	ldp	x20, x19, [sp, #0x40]
    5abc: a94357f6     	ldp	x22, x21, [sp, #0x30]
    5ac0: a9425ff8     	ldp	x24, x23, [sp, #0x20]
    5ac4: a94167fc     	ldp	x28, x25, [sp, #0x10]
    5ac8: a8c57bfd     	ldp	x29, x30, [sp], #0x50
    5acc: d65f03c0     	ret
    5ad0: a9bc7bfd     	stp	x29, x30, [sp, #-0x40]!
    5ad4: a9015ffc     	stp	x28, x23, [sp, #0x10]
    5ad8: a90257f6     	stp	x22, x21, [sp, #0x20]
    5adc: a9034ff4     	stp	x20, x19, [sp, #0x30]
    5ae0: 910003fd     	mov	x29, sp
    5ae4: d10803ff     	sub	sp, sp, #0x200
    5ae8: d00000c8     	adrp	x8, 0x1f000
    5aec: f9470908     	ldr	x8, [x8, #0xe10]
    5af0: b4000428     	cbz	x8, 0x5b74 <zygisk_module_entry+0x9d0>
    5af4: aa0103f5     	mov	x21, x1
    5af8: 910003e1     	mov	x1, sp
    5afc: aa0203f4     	mov	x20, x2
    5b00: d63f0100     	blr	x8
    5b04: 2a0003f3     	mov	w19, w0
    5b08: 37f80960     	tbnz	w0, #0x1f, 0x5c34 <zygisk_module_entry+0xa90>
    5b0c: b4000175     	cbz	x21, 0x5b38 <zygisk_module_entry+0x994>
    5b10: 910003e0     	mov	x0, sp
    5b14: 940001df     	bl	0x6290 <strlen@plt>
    5b18: 528003e8     	mov	w8, #0x1f               // =31
    5b1c: f1007c1f     	cmp	x0, #0x1f
    5b20: 910003e1     	mov	x1, sp
    5b24: 9a883016     	csel	x22, x0, x8, lo
    5b28: aa1503e0     	mov	x0, x21
    5b2c: aa1603e2     	mov	x2, x22
    5b30: 940001e8     	bl	0x62d0 <memcpy@plt>
    5b34: 38366abf     	strb	wzr, [x21, x22]
    5b38: b00000c8     	adrp	x8, 0x1e000
    5b3c: b946f916     	ldr	w22, [x8, #0x6f8]
    5b40: 710006df     	cmp	w22, #0x1
    5b44: 5400014b     	b.lt	0x5b6c <zygisk_module_entry+0x9c8>
    5b48: b00000d5     	adrp	x21, 0x1e000
    5b4c: 911bf2b5     	add	x21, x21, #0x6fc
    5b50: 910003e1     	mov	x1, sp
    5b54: aa1503e0     	mov	x0, x21
    5b58: 940001d2     	bl	0x62a0 <strcmp@plt>
    5b5c: 34000100     	cbz	w0, 0x5b7c <zygisk_module_entry+0x9d8>
    5b60: f10006d6     	subs	x22, x22, #0x1
    5b64: 9102e2b5     	add	x21, x21, #0xb8
    5b68: 54ffff41     	b.ne	0x5b50 <zygisk_module_entry+0x9ac>
    5b6c: aa1f03f5     	mov	x21, xzr
    5b70: 14000004     	b	0x5b80 <zygisk_module_entry+0x9dc>
    5b74: 2a1f03f3     	mov	w19, wzr
    5b78: 1400002f     	b	0x5c34 <zygisk_module_entry+0xa90>
    5b7c: 910172b5     	add	x21, x21, #0x5c
    5b80: b0000020     	adrp	x0, 0xa000
    5b84: f942e001     	ldr	x1, [x0, #0x5c0]
    5b88: 91170000     	add	x0, x0, #0x5c0
    5b8c: d63f0020     	blr	x1
    5b90: d53bd056     	mrs	x22, TPIDR_EL0
    5b94: b8606ac8     	ldr	w8, [x22, x0]
    5b98: 35000248     	cbnz	w8, 0x5be0 <zygisk_module_entry+0xa3c>
    5b9c: 52800028     	mov	w8, #0x1                // =1
    5ba0: b8206ac8     	str	w8, [x22, x0]
    5ba4: f10002bf     	cmp	x21, #0x0
    5ba8: f0ffffc8     	adrp	x8, 0x0
    5bac: 912ed108     	add	x8, x8, #0xbb4
    5bb0: f0ffffc9     	adrp	x9, 0x0
    5bb4: 91292929     	add	x9, x9, #0xa4a
    5bb8: f0ffffc1     	adrp	x1, 0x0
    5bbc: 912ad821     	add	x1, x1, #0xab6
    5bc0: 9a880124     	csel	x4, x9, x8, eq
    5bc4: f0ffffc2     	adrp	x2, 0x0
    5bc8: 91309c42     	add	x2, x2, #0xc27
    5bcc: 910003e3     	mov	x3, sp
    5bd0: aa0003f7     	mov	x23, x0
    5bd4: 52800080     	mov	w0, #0x4                // =4
    5bd8: 940001b6     	bl	0x62b0 <__android_log_print@plt>
    5bdc: b8376adf     	str	wzr, [x22, x23]
    5be0: b40002b4     	cbz	x20, 0x5c34 <zygisk_module_entry+0xa90>
    5be4: b4000295     	cbz	x21, 0x5c34 <zygisk_module_entry+0xa90>
    5be8: aa1503e0     	mov	x0, x21
    5bec: 940001a9     	bl	0x6290 <strlen@plt>
    5bf0: 52800b68     	mov	w8, #0x5b               // =91
    5bf4: f1016c1f     	cmp	x0, #0x5b
    5bf8: aa0003f3     	mov	x19, x0
    5bfc: 9a883016     	csel	x22, x0, x8, lo
    5c00: aa1403e0     	mov	x0, x20
    5c04: aa1503e1     	mov	x1, x21
    5c08: aa1603e2     	mov	x2, x22
    5c0c: 940001b1     	bl	0x62d0 <memcpy@plt>
    5c10: f0ffffc1     	adrp	x1, 0x0
    5c14: 91295021     	add	x1, x1, #0xa54
    5c18: f0ffffc2     	adrp	x2, 0x0
    5c1c: 912e3c42     	add	x2, x2, #0xb8f
    5c20: 910003e3     	mov	x3, sp
    5c24: 52800080     	mov	w0, #0x4                // =4
    5c28: aa1503e4     	mov	x4, x21
    5c2c: 38366a9f     	strb	wzr, [x20, x22]
    5c30: 940001a0     	bl	0x62b0 <__android_log_print@plt>
    5c34: 2a1303e0     	mov	w0, w19
    5c38: 910803ff     	add	sp, sp, #0x200
    5c3c: a9434ff4     	ldp	x20, x19, [sp, #0x30]
    5c40: a94257f6     	ldp	x22, x21, [sp, #0x20]
    5c44: a9415ffc     	ldp	x28, x23, [sp, #0x10]
    5c48: a8c47bfd     	ldp	x29, x30, [sp], #0x40
    5c4c: d65f03c0     	ret
    5c50: a9bd7bfd     	stp	x29, x30, [sp, #-0x30]!
    5c54: f9000bf5     	str	x21, [sp, #0x10]
    5c58: a9024ff4     	stp	x20, x19, [sp, #0x20]
    5c5c: 910003fd     	mov	x29, sp
    5c60: aa0003f3     	mov	x19, x0
    5c64: b0000020     	adrp	x0, 0xa000
    5c68: f942e001     	ldr	x1, [x0, #0x5c0]
    5c6c: 91170000     	add	x0, x0, #0x5c0
    5c70: d63f0020     	blr	x1
    5c74: d53bd054     	mrs	x20, TPIDR_EL0
    5c78: b8606a88     	ldr	w8, [x20, x0]
    5c7c: 350001e8     	cbnz	w8, 0x5cb8 <zygisk_module_entry+0xb14>
    5c80: 52800028     	mov	w8, #0x1                // =1
    5c84: b8206a88     	str	w8, [x20, x0]
    5c88: f100027f     	cmp	x19, #0x0
    5c8c: f0ffffc8     	adrp	x8, 0x0
    5c90: 91308108     	add	x8, x8, #0xc20
    5c94: f0ffffc1     	adrp	x1, 0x0
    5c98: 912ad821     	add	x1, x1, #0xab6
    5c9c: 9a930103     	csel	x3, x8, x19, eq
    5ca0: f0ffffc2     	adrp	x2, 0x0
    5ca4: 91292c42     	add	x2, x2, #0xa4b
    5ca8: aa0003f5     	mov	x21, x0
    5cac: 52800080     	mov	w0, #0x4                // =4
    5cb0: 94000180     	bl	0x62b0 <__android_log_print@plt>
    5cb4: b8356a9f     	str	wzr, [x20, x21]
    5cb8: d00000c8     	adrp	x8, 0x1f000
    5cbc: f9470d01     	ldr	x1, [x8, #0xe18]
    5cc0: b40000c1     	cbz	x1, 0x5cd8 <zygisk_module_entry+0xb34>
    5cc4: aa1303e0     	mov	x0, x19
    5cc8: a9424ff4     	ldp	x20, x19, [sp, #0x20]
    5ccc: f9400bf5     	ldr	x21, [sp, #0x10]
    5cd0: a8c37bfd     	ldp	x29, x30, [sp], #0x30
    5cd4: d61f0020     	br	x1
    5cd8: aa1f03e0     	mov	x0, xzr
    5cdc: a9424ff4     	ldp	x20, x19, [sp, #0x20]
    5ce0: f9400bf5     	ldr	x21, [sp, #0x10]
    5ce4: a8c37bfd     	ldp	x29, x30, [sp], #0x30
    5ce8: d65f03c0     	ret
    5cec: f9400809     	ldr	x9, [x0, #0x10]
    5cf0: b40012a9     	cbz	x9, 0x5f44 <zygisk_module_entry+0xda0>
    5cf4: 79403008     	ldrh	w8, [x0, #0x18]
    5cf8: b4001268     	cbz	x8, 0x5f44 <zygisk_module_entry+0xda0>
    5cfc: d101c3ff     	sub	sp, sp, #0x70
    5d00: a9017bfd     	stp	x29, x30, [sp, #0x10]
    5d04: f90013fb     	str	x27, [sp, #0x20]
    5d08: a90367fa     	stp	x26, x25, [sp, #0x30]
    5d0c: a9045ff8     	stp	x24, x23, [sp, #0x40]
    5d10: a90557f6     	stp	x22, x21, [sp, #0x50]
    5d14: a9064ff4     	stp	x20, x19, [sp, #0x60]
    5d18: 910043fd     	add	x29, sp, #0x10
    5d1c: 91008129     	add	x9, x9, #0x20
    5d20: b85e012a     	ldur	w10, [x9, #-0x20]
    5d24: 7100095f     	cmp	w10, #0x2
    5d28: 540000a0     	b.eq	0x5d3c <zygisk_module_entry+0xb98>
    5d2c: f1000508     	subs	x8, x8, #0x1
    5d30: 9100e129     	add	x9, x9, #0x38
    5d34: 54ffff61     	b.ne	0x5d20 <zygisk_module_entry+0xb7c>
    5d38: 1400007c     	b	0x5f28 <zygisk_module_entry+0xd84>
    5d3c: f9400008     	ldr	x8, [x0]
    5d40: f85f012a     	ldur	x10, [x9, #-0x10]
    5d44: ab08014a     	adds	x10, x10, x8
    5d48: 54000f00     	b.eq	0x5f28 <zygisk_module_entry+0xd84>
    5d4c: f940012b     	ldr	x11, [x9]
    5d50: f100417f     	cmp	x11, #0x10
    5d54: 54000ea3     	b.lo	0x5f28 <zygisk_module_entry+0xd84>
    5d58: d00000cc     	adrp	x12, 0x1f000
    5d5c: d344fd6b     	lsr	x11, x11, #4
    5d60: aa1f03f4     	mov	x20, xzr
    5d64: b94e218d     	ldr	w13, [x12, #0xe20]
    5d68: aa1f03f5     	mov	x21, xzr
    5d6c: aa1f03f3     	mov	x19, xzr
    5d70: aa1f03f6     	mov	x22, xzr
    5d74: aa1f03e9     	mov	x9, xzr
    5d78: aa1f03f7     	mov	x23, xzr
    5d7c: 110005ad     	add	w13, w13, #0x1
    5d80: aa1f03e1     	mov	x1, xzr
    5d84: b90e218d     	str	w13, [x12, #0xe20]
    5d88: 9100214c     	add	x12, x10, #0x8
    5d8c: 528000ea     	mov	w10, #0x7               // =7
    5d90: 14000006     	b	0x5da8 <zygisk_module_entry+0xc04>
    5d94: f940018d     	ldr	x13, [x12]
    5d98: 8b0801b5     	add	x21, x13, x8
    5d9c: f100056b     	subs	x11, x11, #0x1
    5da0: 9100418c     	add	x12, x12, #0x10
    5da4: 54000540     	b.eq	0x5e4c <zygisk_module_entry+0xca8>
    5da8: f85f818d     	ldur	x13, [x12, #-0x8]
    5dac: f1001dbf     	cmp	x13, #0x7
    5db0: 5400014c     	b.gt	0x5dd8 <zygisk_module_entry+0xc34>
    5db4: f10015bf     	cmp	x13, #0x5
    5db8: 5400020c     	b.gt	0x5df8 <zygisk_module_entry+0xc54>
    5dbc: f10009bf     	cmp	x13, #0x2
    5dc0: 54000400     	b.eq	0x5e40 <zygisk_module_entry+0xc9c>
    5dc4: f10015bf     	cmp	x13, #0x5
    5dc8: 54000401     	b.ne	0x5e48 <zygisk_module_entry+0xca4>
    5dcc: f940018d     	ldr	x13, [x12]
    5dd0: 8b0801b4     	add	x20, x13, x8
    5dd4: 17fffff2     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5dd8: f1004dbf     	cmp	x13, #0x13
    5ddc: 540001cc     	b.gt	0x5e14 <zygisk_module_entry+0xc70>
    5de0: f10021bf     	cmp	x13, #0x8
    5de4: 54000260     	b.eq	0x5e30 <zygisk_module_entry+0xc8c>
    5de8: f10029bf     	cmp	x13, #0xa
    5dec: 54fffd81     	b.ne	0x5d9c <zygisk_module_entry+0xbf8>
    5df0: f9400193     	ldr	x19, [x12]
    5df4: 17ffffea     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5df8: f10019bf     	cmp	x13, #0x6
    5dfc: 54fffcc0     	b.eq	0x5d94 <zygisk_module_entry+0xbf0>
    5e00: f1001dbf     	cmp	x13, #0x7
    5e04: 54fffcc1     	b.ne	0x5d9c <zygisk_module_entry+0xbf8>
    5e08: f940018d     	ldr	x13, [x12]
    5e0c: 8b0801b7     	add	x23, x13, x8
    5e10: 17ffffe3     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5e14: f10051bf     	cmp	x13, #0x14
    5e18: 54000100     	b.eq	0x5e38 <zygisk_module_entry+0xc94>
    5e1c: f1005dbf     	cmp	x13, #0x17
    5e20: 54fffbe1     	b.ne	0x5d9c <zygisk_module_entry+0xbf8>
    5e24: f940018d     	ldr	x13, [x12]
    5e28: 8b0801a1     	add	x1, x13, x8
    5e2c: 17ffffdc     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5e30: f9400196     	ldr	x22, [x12]
    5e34: 17ffffda     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5e38: f940018a     	ldr	x10, [x12]
    5e3c: 17ffffd8     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5e40: f9400189     	ldr	x9, [x12]
    5e44: 17ffffd6     	b	0x5d9c <zygisk_module_entry+0xbf8>
    5e48: b5fffaad     	cbnz	x13, 0x5d9c <zygisk_module_entry+0xbf8>
    5e4c: b40006f5     	cbz	x21, 0x5f28 <zygisk_module_entry+0xd84>
    5e50: b40006d4     	cbz	x20, 0x5f28 <zygisk_module_entry+0xd84>
    5e54: b40006b3     	cbz	x19, 0x5f28 <zygisk_module_entry+0xd84>
    5e58: d00000d8     	adrp	x24, 0x1f000
    5e5c: b94e2b19     	ldr	w25, [x24, #0xe28]
    5e60: b4000241     	cbz	x1, 0x5ea8 <zygisk_module_entry+0xd04>
    5e64: b4000229     	cbz	x9, 0x5ea8 <zygisk_module_entry+0xd04>
    5e68: f1001d5f     	cmp	x10, #0x7
    5e6c: 540001e1     	b.ne	0x5ea8 <zygisk_module_entry+0xd04>
    5e70: f9400046     	ldr	x6, [x2]
    5e74: b9400847     	ldr	w7, [x2, #0x8]
    5e78: aa0003fa     	mov	x26, x0
    5e7c: f940084a     	ldr	x10, [x2, #0x10]
    5e80: aa0803e0     	mov	x0, x8
    5e84: aa0203fb     	mov	x27, x2
    5e88: aa0903e2     	mov	x2, x9
    5e8c: aa1503e3     	mov	x3, x21
    5e90: aa1403e4     	mov	x4, x20
    5e94: aa1303e5     	mov	x5, x19
    5e98: f90003ea     	str	x10, [sp]
    5e9c: 9400002c     	bl	0x5f4c <zygisk_module_entry+0xda8>
    5ea0: aa1b03e2     	mov	x2, x27
    5ea4: aa1a03e0     	mov	x0, x26
    5ea8: b4000217     	cbz	x23, 0x5ee8 <zygisk_module_entry+0xd44>
    5eac: b40001f6     	cbz	x22, 0x5ee8 <zygisk_module_entry+0xd44>
    5eb0: f9400008     	ldr	x8, [x0]
    5eb4: f9400046     	ldr	x6, [x2]
    5eb8: aa0003fa     	mov	x26, x0
    5ebc: b9400847     	ldr	w7, [x2, #0x8]
    5ec0: f9400849     	ldr	x9, [x2, #0x10]
    5ec4: aa1703e1     	mov	x1, x23
    5ec8: aa0803e0     	mov	x0, x8
    5ecc: aa1603e2     	mov	x2, x22
    5ed0: aa1503e3     	mov	x3, x21
    5ed4: aa1403e4     	mov	x4, x20
    5ed8: aa1303e5     	mov	x5, x19
    5edc: f90003e9     	str	x9, [sp]
    5ee0: 9400001b     	bl	0x5f4c <zygisk_module_entry+0xda8>
    5ee4: aa1a03e0     	mov	x0, x26
    5ee8: b94e2b08     	ldr	w8, [x24, #0xe28]
    5eec: 6b190103     	subs	w3, w8, w25
    5ef0: 540001cd     	b.le	0x5f28 <zygisk_module_entry+0xd84>
    5ef4: f9400408     	ldr	x8, [x0, #0x8]
    5ef8: f0ffffc4     	adrp	x4, 0x0
    5efc: 912eac84     	add	x4, x4, #0xbab
    5f00: b4000088     	cbz	x8, 0x5f10 <zygisk_module_entry+0xd6c>
    5f04: 39400109     	ldrb	w9, [x8]
    5f08: 7100013f     	cmp	w9, #0x0
    5f0c: 9a880084     	csel	x4, x4, x8, eq
    5f10: f0ffffc1     	adrp	x1, 0x0
    5f14: 91295021     	add	x1, x1, #0xa54
    5f18: f0ffffc2     	adrp	x2, 0x0
    5f1c: 912f5c42     	add	x2, x2, #0xbd7
    5f20: 52800080     	mov	w0, #0x4                // =4
    5f24: 940000e3     	bl	0x62b0 <__android_log_print@plt>
    5f28: a9464ff4     	ldp	x20, x19, [sp, #0x60]
    5f2c: f94013fb     	ldr	x27, [sp, #0x20]
    5f30: a94557f6     	ldp	x22, x21, [sp, #0x50]
    5f34: a9445ff8     	ldp	x24, x23, [sp, #0x40]
    5f38: a94367fa     	ldp	x26, x25, [sp, #0x30]
    5f3c: a9417bfd     	ldp	x29, x30, [sp, #0x10]
    5f40: 9101c3ff     	add	sp, sp, #0x70
    5f44: 2a1f03e0     	mov	w0, wzr
    5f48: d65f03c0     	ret
    5f4c: a9ba7bfd     	stp	x29, x30, [sp, #-0x60]!
    5f50: a9016ffc     	stp	x28, x27, [sp, #0x10]
    5f54: a90267fa     	stp	x26, x25, [sp, #0x20]
    5f58: a9035ff8     	stp	x24, x23, [sp, #0x30]
    5f5c: a90457f6     	stp	x22, x21, [sp, #0x40]
    5f60: a9054ff4     	stp	x20, x19, [sp, #0x50]
    5f64: 910003fd     	mov	x29, sp
    5f68: d10983ff     	sub	sp, sp, #0x260
    5f6c: f100605f     	cmp	x2, #0x18
    5f70: a90317e3     	stp	x3, x5, [sp, #0x30]
    5f74: f9000fe0     	str	x0, [sp, #0x18]
    5f78: 54000122     	b.hs	0x5f9c <zygisk_module_entry+0xdf8>
    5f7c: 910983ff     	add	sp, sp, #0x260
    5f80: a9454ff4     	ldp	x20, x19, [sp, #0x50]
    5f84: a94457f6     	ldp	x22, x21, [sp, #0x40]
    5f88: a9435ff8     	ldp	x24, x23, [sp, #0x30]
    5f8c: a94267fa     	ldp	x26, x25, [sp, #0x20]
    5f90: a9416ffc     	ldp	x28, x27, [sp, #0x10]
    5f94: a8c67bfd     	ldp	x29, x30, [sp], #0x60
    5f98: d65f03c0     	ret
    5f9c: b201f3e8     	mov	x8, #-0x5555555555555556 // =-6148914691236517206
    5fa0: f94033a9     	ldr	x9, [x29, #0x60]
    5fa4: 2a0703eb     	mov	w11, w7
    5fa8: f2955568     	movk	x8, #0xaaab
    5fac: 910020ca     	add	x10, x6, #0x8
    5fb0: 2a0703f3     	mov	w19, w7
    5fb4: 9bc87c48     	umulh	x8, x2, x8
    5fb8: aa0403f5     	mov	x21, x4
    5fbc: aa0103f7     	mov	x23, x1
    5fc0: aa1f03fa     	mov	x26, xzr
    5fc4: a9022fea     	stp	x10, x11, [sp, #0x20]
    5fc8: 52800318     	mov	w24, #0x18              // =24
    5fcc: cb0903ea     	neg	x10, x9
    5fd0: 91001d29     	add	x9, x9, #0x7
    5fd4: a900abe9     	stp	x9, x10, [sp, #0x8]
    5fd8: d344fd19     	lsr	x25, x8, #4
    5fdc: 14000008     	b	0x5ffc <zygisk_module_entry+0xe58>
    5fe0: d00000c9     	adrp	x9, 0x1f000
    5fe4: b94e3128     	ldr	w8, [x9, #0xe30]
    5fe8: 11000508     	add	w8, w8, #0x1
    5fec: b90e3128     	str	w8, [x9, #0xe30]
    5ff0: 9100075a     	add	x26, x26, #0x1
    5ff4: eb19035f     	cmp	x26, x25
    5ff8: 54fffc20     	b.eq	0x5f7c <zygisk_module_entry+0xdd8>
    5ffc: 9b185f5b     	madd	x27, x26, x24, x23
    6000: f9400768     	ldr	x8, [x27, #0x8]
    6004: 51100d09     	sub	w9, w8, #0x403
    6008: 3100093f     	cmn	w9, #0x2
    600c: 54ffff23     	b.lo	0x5ff0 <zygisk_module_entry+0xe4c>
    6010: d360fd08     	lsr	x8, x8, #32
    6014: a94327eb     	ldp	x11, x9, [sp, #0x30]
    6018: b00000ca     	adrp	x10, 0x1f000
    601c: 9bb87d08     	umull	x8, w8, w24
    6020: b868697c     	ldr	w28, [x11, x8]
    6024: b94e2548     	ldr	w8, [x10, #0xe24]
    6028: eb1c013f     	cmp	x9, x28
    602c: 11000508     	add	w8, w8, #0x1
    6030: 7a418a68     	ccmp	w19, #0x1, #0x8, hi
    6034: b90e2548     	str	w8, [x10, #0xe24]
    6038: 54fffdcb     	b.lt	0x5ff0 <zygisk_module_entry+0xe4c>
    603c: a9425bf4     	ldp	x20, x22, [sp, #0x20]
    6040: f85f8281     	ldur	x1, [x20, #-0x8]
    6044: 8b1c02a0     	add	x0, x21, x28
    6048: 94000096     	bl	0x62a0 <strcmp@plt>
    604c: 340000a0     	cbz	w0, 0x6060 <zygisk_module_entry+0xebc>
    6050: f10006d6     	subs	x22, x22, #0x1
    6054: 91006294     	add	x20, x20, #0x18
    6058: 54ffff41     	b.ne	0x6040 <zygisk_module_entry+0xe9c>
    605c: 17ffffe5     	b	0x5ff0 <zygisk_module_entry+0xe4c>
    6060: f9400368     	ldr	x8, [x27]
    6064: f9400fe9     	ldr	x9, [sp, #0x18]
    6068: 8b09011b     	add	x27, x8, x9
    606c: f9400289     	ldr	x9, [x20]
    6070: f9400368     	ldr	x8, [x27]
    6074: eb09011f     	cmp	x8, x9
    6078: 54000600     	b.eq	0x6138 <zygisk_module_entry+0xf94>
    607c: b4fffb28     	cbz	x8, 0x5fe0 <zygisk_module_entry+0xe3c>
    6080: f9400689     	ldr	x9, [x20, #0x8]
    6084: f940012a     	ldr	x10, [x9]
    6088: b500004a     	cbnz	x10, 0x6090 <zygisk_module_entry+0xeec>
    608c: f9000128     	str	x8, [x9]
    6090: d0ffffc0     	adrp	x0, 0x0
    6094: 91270000     	add	x0, x0, #0x9c0
    6098: d0ffffc1     	adrp	x1, 0x0
    609c: 912ec821     	add	x1, x1, #0xbb2
    60a0: 94000098     	bl	0x6300 <fopen@plt>
    60a4: d0ffffd6     	adrp	x22, 0x0
    60a8: 912ef6d6     	add	x22, x22, #0xbbd
    60ac: b40005a0     	cbz	x0, 0x6160 <zygisk_module_entry+0xfbc>
    60b0: aa0003fc     	mov	x28, x0
    60b4: 910163e0     	add	x0, sp, #0x58
    60b8: 52804001     	mov	w1, #0x200              // =512
    60bc: aa1c03e2     	mov	x2, x28
    60c0: 94000094     	bl	0x6310 <fgets@plt>
    60c4: b4000440     	cbz	x0, 0x614c <zygisk_module_entry+0xfa8>
    60c8: 910163e0     	add	x0, sp, #0x58
    60cc: 910143e2     	add	x2, sp, #0x50
    60d0: 910123e3     	add	x3, sp, #0x48
    60d4: 910103e4     	add	x4, sp, #0x40
    60d8: aa1603e1     	mov	x1, x22
    60dc: f90023ff     	str	xzr, [sp, #0x40]
    60e0: 94000090     	bl	0x6320 <sscanf@plt>
    60e4: 71000c1f     	cmp	w0, #0x3
    60e8: 54fffe61     	b.ne	0x60b4 <zygisk_module_entry+0xf10>
    60ec: f9402be8     	ldr	x8, [sp, #0x50]
    60f0: eb08037f     	cmp	x27, x8
    60f4: 54fffe03     	b.lo	0x60b4 <zygisk_module_entry+0xf10>
    60f8: f94027e8     	ldr	x8, [sp, #0x48]
    60fc: eb08037f     	cmp	x27, x8
    6100: 54fffda2     	b.hs	0x60b4 <zygisk_module_entry+0xf10>
    6104: 394103e8     	ldrb	w8, [sp, #0x40]
    6108: 394107e9     	ldrb	w9, [sp, #0x41]
    610c: 5280004a     	mov	w10, #0x2               // =2
    6110: 7101c91f     	cmp	w8, #0x72
    6114: 1a9f17e8     	cset	w8, eq
    6118: 1a8a154a     	cinc	w10, w10, eq
    611c: 7101dd3f     	cmp	w9, #0x77
    6120: 39410be9     	ldrb	w9, [sp, #0x42]
    6124: 1a880148     	csel	w8, w10, w8, eq
    6128: 321e010a     	orr	w10, w8, #0x4
    612c: 7101e13f     	cmp	w9, #0x78
    6130: 1a880148     	csel	w8, w10, w8, eq
    6134: 14000007     	b	0x6150 <zygisk_module_entry+0xfac>
    6138: b00000c9     	adrp	x9, 0x1f000
    613c: b94e2d28     	ldr	w8, [x9, #0xe2c]
    6140: 11000508     	add	w8, w8, #0x1
    6144: b90e2d28     	str	w8, [x9, #0xe2c]
    6148: 17ffffaa     	b	0x5ff0 <zygisk_module_entry+0xe4c>
    614c: 12800008     	mov	w8, #-0x1               // =-1
    6150: aa1c03e0     	mov	x0, x28
    6154: b90007e8     	str	w8, [sp, #0x4]
    6158: 94000076     	bl	0x6330 <fclose@plt>
    615c: 14000003     	b	0x6168 <zygisk_module_entry+0xfc4>
    6160: 12800008     	mov	w8, #-0x1               // =-1
    6164: b90007e8     	str	w8, [sp, #0x4]
    6168: a940a7e8     	ldp	x8, x9, [sp, #0x8]
    616c: 52800062     	mov	w2, #0x3                // =3
    6170: 8b1b0108     	add	x8, x8, x27
    6174: 8a09037c     	and	x28, x27, x9
    6178: 8a090108     	and	x8, x8, x9
    617c: aa1c03e0     	mov	x0, x28
    6180: cb1c0101     	sub	x1, x8, x28
    6184: aa0103f6     	mov	x22, x1
    6188: 9400006e     	bl	0x6340 <mprotect@plt>
    618c: 35fff2a0     	cbnz	w0, 0x5fe0 <zygisk_module_entry+0xe3c>
    6190: b94007e9     	ldr	w9, [sp, #0x4]
    6194: f9400288     	ldr	x8, [x20]
    6198: aa1c03e0     	mov	x0, x28
    619c: aa1603e1     	mov	x1, x22
    61a0: 7100013f     	cmp	w9, #0x0
    61a4: f9000368     	str	x8, [x27]
    61a8: 1a9fa522     	csinc	w2, w9, wzr, ge
    61ac: 94000065     	bl	0x6340 <mprotect@plt>
    61b0: b00000c9     	adrp	x9, 0x1f000
    61b4: b94e2928     	ldr	w8, [x9, #0xe28]
    61b8: 11000508     	add	w8, w8, #0x1
    61bc: b90e2928     	str	w8, [x9, #0xe28]
    61c0: 17ffff8c     	b	0x5ff0 <zygisk_module_entry+0xe4c>

Disassembly of section .plt:

00000000000061d0 <.plt>:
    61d0: a9bf7bf0     	stp	x16, x30, [sp, #-0x10]!
    61d4: 90000030     	adrp	x16, 0xa000
    61d8: f942f211     	ldr	x17, [x16, #0x5e0]
    61dc: 91178210     	add	x16, x16, #0x5e0
    61e0: d61f0220     	br	x17
    61e4: d503201f     	nop
    61e8: d503201f     	nop
    61ec: d503201f     	nop

00000000000061f0 <__cxa_finalize@plt>:
    61f0: 90000030     	adrp	x16, 0xa000
    61f4: f942f611     	ldr	x17, [x16, #0x5e8]
    61f8: 9117a210     	add	x16, x16, #0x5e8
    61fc: d61f0220     	br	x17

0000000000006200 <__cxa_atexit@plt>:
    6200: 90000030     	adrp	x16, 0xa000
    6204: f942fa11     	ldr	x17, [x16, #0x5f0]
    6208: 9117c210     	add	x16, x16, #0x5f0
    620c: d61f0220     	br	x17

0000000000006210 <__register_atfork@plt>:
    6210: 90000030     	adrp	x16, 0xa000
    6214: f942fe11     	ldr	x17, [x16, #0x5f8]
    6218: 9117e210     	add	x16, x16, #0x5f8
    621c: d61f0220     	br	x17

0000000000006220 <__cxa_guard_acquire@plt>:
    6220: 90000030     	adrp	x16, 0xa000
    6224: f9430211     	ldr	x17, [x16, #0x600]
    6228: 91180210     	add	x16, x16, #0x600
    622c: d61f0220     	br	x17

0000000000006230 <__cxa_guard_release@plt>:
    6230: 90000030     	adrp	x16, 0xa000
    6234: f9430611     	ldr	x17, [x16, #0x608]
    6238: 91182210     	add	x16, x16, #0x608
    623c: d61f0220     	br	x17

0000000000006240 <openat@plt>:
    6240: 90000030     	adrp	x16, 0xa000
    6244: f9430a11     	ldr	x17, [x16, #0x610]
    6248: 91184210     	add	x16, x16, #0x610
    624c: d61f0220     	br	x17

0000000000006250 <close@plt>:
    6250: 90000030     	adrp	x16, 0xa000
    6254: f9430e11     	ldr	x17, [x16, #0x618]
    6258: 91186210     	add	x16, x16, #0x618
    625c: d61f0220     	br	x17

0000000000006260 <open@plt>:
    6260: 90000030     	adrp	x16, 0xa000
    6264: f9431211     	ldr	x17, [x16, #0x620]
    6268: 91188210     	add	x16, x16, #0x620
    626c: d61f0220     	br	x17

0000000000006270 <read@plt>:
    6270: 90000030     	adrp	x16, 0xa000
    6274: f9431611     	ldr	x17, [x16, #0x628]
    6278: 9118a210     	add	x16, x16, #0x628
    627c: d61f0220     	br	x17

0000000000006280 <strchr@plt>:
    6280: 90000030     	adrp	x16, 0xa000
    6284: f9431a11     	ldr	x17, [x16, #0x630]
    6288: 9118c210     	add	x16, x16, #0x630
    628c: d61f0220     	br	x17

0000000000006290 <strlen@plt>:
    6290: 90000030     	adrp	x16, 0xa000
    6294: f9431e11     	ldr	x17, [x16, #0x638]
    6298: 9118e210     	add	x16, x16, #0x638
    629c: d61f0220     	br	x17

00000000000062a0 <strcmp@plt>:
    62a0: 90000030     	adrp	x16, 0xa000
    62a4: f9432211     	ldr	x17, [x16, #0x640]
    62a8: 91190210     	add	x16, x16, #0x640
    62ac: d61f0220     	br	x17

00000000000062b0 <__android_log_print@plt>:
    62b0: 90000030     	adrp	x16, 0xa000
    62b4: f9432611     	ldr	x17, [x16, #0x648]
    62b8: 91192210     	add	x16, x16, #0x648
    62bc: d61f0220     	br	x17

00000000000062c0 <memset@plt>:
    62c0: 90000030     	adrp	x16, 0xa000
    62c4: f9432a11     	ldr	x17, [x16, #0x650]
    62c8: 91194210     	add	x16, x16, #0x650
    62cc: d61f0220     	br	x17

00000000000062d0 <memcpy@plt>:
    62d0: 90000030     	adrp	x16, 0xa000
    62d4: f9432e11     	ldr	x17, [x16, #0x658]
    62d8: 91196210     	add	x16, x16, #0x658
    62dc: d61f0220     	br	x17

00000000000062e0 <sysconf@plt>:
    62e0: 90000030     	adrp	x16, 0xa000
    62e4: f9433211     	ldr	x17, [x16, #0x660]
    62e8: 91198210     	add	x16, x16, #0x660
    62ec: d61f0220     	br	x17

00000000000062f0 <dl_iterate_phdr@plt>:
    62f0: 90000030     	adrp	x16, 0xa000
    62f4: f9433611     	ldr	x17, [x16, #0x668]
    62f8: 9119a210     	add	x16, x16, #0x668
    62fc: d61f0220     	br	x17

0000000000006300 <fopen@plt>:
    6300: 90000030     	adrp	x16, 0xa000
    6304: f9433a11     	ldr	x17, [x16, #0x670]
    6308: 9119c210     	add	x16, x16, #0x670
    630c: d61f0220     	br	x17

0000000000006310 <fgets@plt>:
    6310: 90000030     	adrp	x16, 0xa000
    6314: f9433e11     	ldr	x17, [x16, #0x678]
    6318: 9119e210     	add	x16, x16, #0x678
    631c: d61f0220     	br	x17

0000000000006320 <sscanf@plt>:
    6320: 90000030     	adrp	x16, 0xa000
    6324: f9434211     	ldr	x17, [x16, #0x680]
    6328: 911a0210     	add	x16, x16, #0x680
    632c: d61f0220     	br	x17

0000000000006330 <fclose@plt>:
    6330: 90000030     	adrp	x16, 0xa000
    6334: f9434611     	ldr	x17, [x16, #0x688]
    6338: 911a2210     	add	x16, x16, #0x688
    633c: d61f0220     	br	x17

0000000000006340 <mprotect@plt>:
    6340: 90000030     	adrp	x16, 0xa000
    6344: f9434a11     	ldr	x17, [x16, #0x690]
    6348: 911a4210     	add	x16, x16, #0x690
    634c: d61f0220     	br	x17
