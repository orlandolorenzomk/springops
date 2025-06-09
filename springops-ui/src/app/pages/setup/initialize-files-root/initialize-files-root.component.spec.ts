import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitializeFilesRootComponent } from './initialize-files-root.component';

describe('InitializeFilesRootComponent', () => {
  let component: InitializeFilesRootComponent;
  let fixture: ComponentFixture<InitializeFilesRootComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [InitializeFilesRootComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(InitializeFilesRootComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
