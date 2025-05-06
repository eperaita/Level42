import SwiftUI
import AVKit
import shared

struct LoginView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    @State private var videoFinished = false
    @State private var showLoginButton = false
    @State private var player: AVPlayer?

    var body: some View {
        ZStack {
            // Video de fondo
            if let player = player {
                VideoPlayerController(
                    player: player,
                    onVideoFinished: {
                        videoFinished = true
                        showLoginButton = true
                    }
                )
                .edgesIgnoringSafeArea(.all)
            }

            // Contenido overlay
            VStack {
                Spacer()

                if showLoginButton {
                    LoginButton(
                        action: {
                            startOAuthFlow()
                        }
                    )
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
                    .padding(.trailing, 10)
                    .padding(.bottom, 50)
                }
            }
        }
        .navigationBarBackButtonHidden(true) // Oculta el botón Back
        .navigationBarHidden(true) // Oculta toda la barra de navegación
        .onAppear {
            setupVideoPlayer()
        }
        .onDisappear {
            player?.pause()
        }
    }

    private func setupVideoPlayer() {
        guard let url = Bundle.main.url(forResource: "compose-resources/loginvideo", withExtension: "mp4") else {
            showLoginButton = true
            return
        }

        let asset = AVAsset(url: url)
        let playerItem = AVPlayerItem(asset: asset)
        player = AVPlayer(playerItem: playerItem)
        player?.isMuted = true

        // Configurar para mantener el último frame al finalizar
        player?.actionAtItemEnd = .pause

        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: player?.currentItem,
            queue: .main
        ) { _ in
            videoFinished = true
            showLoginButton = true
        }

        player?.play()
    }

    private func startOAuthFlow() {
        navigationPath.append("loading")
        let oauthURL = Api42().getURI()
        print("Opening OAuth URL: \(oauthURL)")

        if let url = URL(string: oauthURL) {
            UIApplication.shared.open(url)
        }
    }
}

// Componente reutilizable para el botón
struct LoginButton: View {
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            Text("LOG\nIN")
                .font(.system(size: 16, weight: .bold, design: .default))
                .multilineTextAlignment(.center)
                .frame(width: 100, height: 100)
                .background(Color(red: 1.0, green: 0.988, blue: 0.0))
                .foregroundColor(.black)
                .clipShape(Circle())
                .overlay(
                    Circle()
                        .stroke(Color.black, lineWidth: 2)
                )
        }
    }
}

// Wrapper para AVPlayerViewController
struct VideoPlayerController: UIViewControllerRepresentable {
    let player: AVPlayer
    let onVideoFinished: () -> Void

    func makeUIViewController(context: Context) -> AVPlayerViewController {
        let controller = AVPlayerViewController()
        controller.player = player
        controller.showsPlaybackControls = false
        controller.videoGravity = .resizeAspectFill

        // Configurar notificación de fin de video
        NotificationCenter.default.addObserver(
            context.coordinator,
            selector: #selector(Coordinator.videoDidFinish),
            name: .AVPlayerItemDidPlayToEndTime,
            object: player.currentItem
        )

        return controller
    }

    func updateUIViewController(_ uiViewController: AVPlayerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(onVideoFinished: onVideoFinished)
    }

    class Coordinator: NSObject {
        var onVideoFinished: () -> Void

        init(onVideoFinished: @escaping () -> Void) {
            self.onVideoFinished = onVideoFinished
        }

        @objc func videoDidFinish() {
            onVideoFinished()
        }
    }
}