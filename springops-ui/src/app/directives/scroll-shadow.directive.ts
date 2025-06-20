import { AfterViewInit, Directive, ElementRef, Renderer2 } from '@angular/core';

@Directive({
  selector: '[stickyShadow]',
})
export class StickyShadowDirective implements AfterViewInit {
  constructor(private el: ElementRef, private renderer: Renderer2) {}

  ngAfterViewInit() {
    const header = this.el.nativeElement;

    // Create sentinel
    const sentinel = this.renderer.createElement('div');
    this.renderer.setStyle(sentinel, 'height', '1px');
    this.renderer.setStyle(sentinel, 'width', '100%');
    this.renderer.insertBefore(header.parentNode, sentinel, header);

    // Observe sentinel
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          this.renderer.removeClass(header, 'stuck');
        } else {
          this.renderer.addClass(header, 'stuck');
        }
      },
      { threshold: [1] }
    );

    observer.observe(sentinel);
  }
}
