	.globl _main
_main:
	push	%rbp
	movq	%rsp, %rbp
	subq	$4, %rsp
	movl	$1, %eax
	movl	%eax,0(%rbp)
	subq	$4, %rsp
	movl	$2, %eax
	movl	%eax,-4(%rbp)
	movl	0(%rbp), %eax
	push	%rax
	movl	-4(%rbp), %eax
	pop	%rbx
	addl	%ebx, %eax
	movq	%rbp, %rsp
	popq	%rbp
	ret
