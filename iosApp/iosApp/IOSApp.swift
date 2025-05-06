import SwiftUI

@main
struct iOSApp: App {

    //Esto permite que el Viewmodel solo existe mientras la vista que lo crea esté activa
    @StateObject private var viewModel = ProfileViewModel()

    var body: some Scene {
        WindowGroup {

            AppView() //Es como App.kt en android, la vista principal
                .environmentObject(viewModel) // Inyecta el ViewModel en el entorno
        }
    }
}