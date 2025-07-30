Pod::Spec.new do |s|
  s.name           = 'ExpoSmsManager'
  s.version        = '1.0.0'
  s.summary        = 'Comprehensive SMS/MMS management for Expo applications'
  s.description    = 'A native module that provides comprehensive SMS and MMS functionality for Expo applications'
  s.author         = ''
  s.homepage       = 'https://github.com/your-username/expo-sms-manager'
  s.platforms      = { :ios => '13.4', :tvos => '13.4' }
  s.source         = { git: '' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'

  # Swift/Objective-C compatibility
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_COMPILATION_MODE' => 'wholemodule'
  }

  s.source_files = "**/*.{h,m,mm,swift,hpp,cpp}"
end
