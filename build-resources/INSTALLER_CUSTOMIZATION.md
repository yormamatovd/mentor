# Windows Installer Customization Guide

## Current Implementation

The build script (`build-scripts/build-windows.ps1`) creates a Windows MSI installer with the following features:

### Included Features
- ✅ **Embedded JRE**: End users don't need Java installed
- ✅ **Application Description**: Shows in installer and control panel
- ✅ **Directory Chooser**: Users can select installation directory
- ✅ **Start Menu Shortcut**: Automatically created
- ✅ **Desktop Shortcut**: Automatically created
- ✅ **Application Icon**: Automatically included (when icon.ico is present)
- ✅ **Per-User Installation**: Doesn't require admin privileges
- ✅ **Menu Group**: Organized under "Algo" in Start Menu

## Advanced Customization (Optional)

### Custom Installer Delay and Progress

The requirement to "slow down installer to 15 seconds with progress bar" requires custom WiX XML. Here's how to implement it:

#### Step 1: Create WiX UI Extension File

Create `build-resources/InstallerUI.wxs`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
  <Fragment>
    <UI>
      <!-- Add custom progress messages -->
      <ProgressText Action="InstallFiles">Installing application files...</ProgressText>
      <ProgressText Action="CreateShortcuts">Creating shortcuts...</ProgressText>
      <ProgressText Action="RegisterProduct">Registering application...</ProgressText>
      
      <!-- Custom property for installation delay -->
      <Property Id="INSTALLATION_DELAY" Value="15" />
    </UI>
    
    <!-- Custom action for delay -->
    <CustomAction Id="DelayInstallation"
                  Execute="deferred"
                  Return="check"
                  Script="vbscript">
      <![CDATA[
        Dim delaySeconds
        delaySeconds = Session.Property("INSTALLATION_DELAY")
        If delaySeconds = "" Then delaySeconds = 15
        
        ' Create progress bar steps
        Set progressRecord = Installer.CreateRecord(3)
        progressRecord.StringData(1) = "DelayInstallation"
        progressRecord.StringData(2) = "Preparing installation environment..."
        progressRecord.IntegerData(3) = CInt(delaySeconds)
        Session.Message &H0B000000, progressRecord
        
        ' Delay with progress updates
        Dim i
        For i = 1 To delaySeconds
          WScript.Sleep 1000
          Set tickRecord = Installer.CreateRecord(2)
          tickRecord.IntegerData(1) = 2
          tickRecord.IntegerData(2) = 1
          Session.Message &H0A000000, tickRecord
        Next
      ]]>
    </CustomAction>
    
    <InstallExecuteSequence>
      <Custom Action="DelayInstallation" After="InstallFiles">NOT Installed</Custom>
    </InstallExecuteSequence>
  </Fragment>
</Wix>
```

#### Step 2: Create Information Screen

Create `build-resources/InfoDialog.wxs`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
  <Fragment>
    <UI>
      <Dialog Id="InfoDlg" Width="370" Height="270" Title="About Mentor">
        <Control Id="Title" Type="Text" X="15" Y="6" Width="200" Height="15" Transparent="yes" NoPrefix="yes">
          <Text>{\WixUI_Font_Title}About Mentor</Text>
        </Control>
        
        <Control Id="BannerBitmap" Type="Bitmap" X="0" Y="0" Width="370" Height="44" TabSkip="no" Text="!(loc.InfoBannerBitmap)" />
        <Control Id="BannerLine" Type="Line" X="0" Y="44" Width="370" Height="0" />
        
        <Control Id="Description" Type="ScrollableText" X="20" Y="60" Width="330" Height="150" Sunken="yes" TabSkip="no">
          <Text>Mentor - Student Management Platform

Features:
• Student enrollment and profile management
• Group and class organization  
• Payment tracking and financial records
• Schedule management
• Attendance tracking
• Performance analytics

This software streamlines administrative tasks for educational institutions.

Version: 1.0.0
Vendor: Algo
License: Proprietary

For support, visit: https://example.com/support
          </Text>
        </Control>
        
        <Control Id="BottomLine" Type="Line" X="0" Y="234" Width="370" Height="0" />
        
        <Control Id="Back" Type="PushButton" X="180" Y="243" Width="56" Height="17" Text="!(loc.WixUIBack)">
          <Publish Event="NewDialog" Value="WelcomeDlg">1</Publish>
        </Control>
        
        <Control Id="Next" Type="PushButton" X="236" Y="243" Width="56" Height="17" Default="yes" Text="!(loc.WixUINext)">
          <Publish Event="NewDialog" Value="LicenseAgreementDlg">1</Publish>
        </Control>
        
        <Control Id="Cancel" Type="PushButton" X="304" Y="243" Width="56" Height="17" Cancel="yes" Text="!(loc.WixUICancel)">
          <Publish Event="SpawnDialog" Value="CancelDlg">1</Publish>
        </Control>
      </Dialog>
      
      <!-- Hook into install sequence -->
      <Publish Dialog="WelcomeDlg" Control="Next" Event="NewDialog" Value="InfoDlg" Order="2">1</Publish>
    </UI>
  </Fragment>
</Wix>
```

#### Step 3: Desktop Shortcut with Checkbox

Create `build-resources/ShortcutOptions.wxs`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
  <Fragment>
    <!-- Property for desktop shortcut (default: 1 = checked) -->
    <Property Id="INSTALLDESKTOPSHORTCUT" Value="1" />
    
    <UI>
      <Dialog Id="CustomizeDlg" Width="370" Height="270" Title="Customize Installation">
        <Control Id="Title" Type="Text" X="15" Y="6" Width="200" Height="15" Transparent="yes" NoPrefix="yes">
          <Text>{\WixUI_Font_Title}Customize Installation</Text>
        </Control>
        
        <Control Id="Subtitle" Type="Text" X="20" Y="30" Width="330" Height="15" Transparent="yes" NoPrefix="yes">
          <Text>Choose installation options</Text>
        </Control>
        
        <Control Id="DesktopShortcutCheckbox" Type="CheckBox" X="20" Y="60" Width="330" Height="17" Property="INSTALLDESKTOPSHORTCUT" CheckBoxValue="1" Text="Create desktop shortcut">
          <Condition Action="disable">0</Condition>
        </Control>
        
        <Control Id="StartMenuShortcutCheckbox" Type="CheckBox" X="20" Y="85" Width="330" Height="17" Property="INSTALLSTARTMENUSHORTCUT" CheckBoxValue="1" Text="Create Start Menu shortcut" Disabled="yes">
          <Condition Action="disable">1</Condition>
        </Control>
        
        <Control Id="Description" Type="Text" X="35" Y="105" Width="315" Height="40" Transparent="yes">
          <Text>Note: Start Menu shortcuts are always created to ensure you can launch the application.</Text>
        </Control>
        
        <Control Id="Back" Type="PushButton" X="180" Y="243" Width="56" Height="17" Text="!(loc.WixUIBack)">
          <Publish Event="NewDialog" Value="InstallDirDlg">1</Publish>
        </Control>
        
        <Control Id="Next" Type="PushButton" X="236" Y="243" Width="56" Height="17" Default="yes" Text="!(loc.WixUINext)">
          <Publish Event="NewDialog" Value="VerifyReadyDlg">1</Publish>
        </Control>
        
        <Control Id="Cancel" Type="PushButton" X="304" Y="243" Width="56" Height="17" Cancel="yes" Text="!(loc.WixUICancel)">
          <Publish Event="SpawnDialog" Value="CancelDlg">1</Publish>
        </Control>
      </Dialog>
      
      <!-- Insert into dialog sequence -->
      <Publish Dialog="InstallDirDlg" Control="Next" Event="NewDialog" Value="CustomizeDlg" Order="4">1</Publish>
    </UI>
    
    <!-- Desktop shortcut component -->
    <DirectoryRef Id="DesktopFolder">
      <Component Id="DesktopShortcutComponent" Guid="YOUR-GUID-HERE">
        <Condition>INSTALLDESKTOPSHORTCUT = 1</Condition>
        <Shortcut Id="DesktopShortcut"
                  Name="Mentor"
                  Description="Student management and teaching platform"
                  Target="[INSTALLDIR]Mentor.exe"
                  WorkingDirectory="INSTALLDIR" />
        <RegistryValue Root="HKCU" Key="Software\Algo\Mentor" Name="DesktopShortcut" Type="integer" Value="1" KeyPath="yes"/>
      </Component>
    </DirectoryRef>
  </Fragment>
</Wix>
```

#### Step 4: Compile Custom WiX Files

To use these custom WiX files with jpackage:

1. Install WiX Toolset: https://wixtoolset.org/
2. Place all .wxs files in `build-resources/`
3. Compile them:
   ```powershell
   candle.exe -arch x64 InstallerUI.wxs InfoDialog.wxs ShortcutOptions.wxs
   light.exe -out CustomInstallerUI.wixlib InstallerUI.wixobj InfoDialog.wixobj ShortcutOptions.wixobj
   ```
4. Modify `build-windows.ps1` to use the custom WiX library:
   ```powershell
   $jpackageArgs += @("--resource-dir", "$PROJECT_ROOT\build-resources")
   ```

### Important Notes

⚠️ **Installation Delay**: Artificially slowing down installers is not recommended as it:
- Frustrates users who expect fast installations
- May be perceived as a bug or hanging installer
- Violates UX best practices
- Could cause timeout issues on slower systems

If you need to show information during installation, use the information dialog instead of a delay.

## Testing the Installer

After building with custom options:

```powershell
# Build with customizations
.\build-scripts\build-windows.ps1

# Test installer
.\target\installer\Mentor-1.0.0.msi

# Test silent install
msiexec /i "target\installer\Mentor-1.0.0.msi" /quiet /log install.log

# Test with properties
msiexec /i "target\installer\Mentor-1.0.0.msi" INSTALLDESKTOPSHORTCUT=0 /log install.log
```

## Troubleshooting

### Installer appears too fast
- This is normal and desired behavior
- Users prefer fast installations
- Consider showing information in the app's first launch instead

### Desktop shortcut not created
- Check if `INSTALLDESKTOPSHORTCUT` property is set correctly
- Verify the WiX component condition
- Check installer logs: `msiexec /i installer.msi /log install.log`

### Custom dialogs not showing
- Ensure WiX files are properly compiled
- Check that dialog sequence is correct
- Verify resource directory is passed to jpackage
